package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisObject;
import jedis.util.MessageConstant;
import jedis.util.Sds;

public class DeleteHandle implements CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl)
			throws UnsupportedOperationException {
		int keyCount = cl.getArgc();
		int deletedCount = 0;
		int curDB = client.getCurrntDB();
		for(int i = 0; i < keyCount;++i){
			Sds key = new Sds(cl.getArg(i));
			Server.removeIfExpired(key, curDB);
			if(Server.inUseDatabases[curDB].remove(key)){
				++deletedCount;
			}
		}
		if(Server.aofState == AofState.AOF_ON){
			Server.aof.put(cl,curDB);
		}
		if(deletedCount < MessageConstant.NUMBER_COUNT){
			return MessageConstant.NUMBERS[deletedCount];
		}
		return new Sds(Integer.toString(deletedCount));
	}

}
