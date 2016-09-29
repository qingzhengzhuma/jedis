package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisClient;
import jedis.util.JedisObject;
import jedis.util.MessageConstant;
import jedis.util.Sds;

public class DeleteHandle implements CommandHandler {

	@Override
	public JedisObject execute(JedisDB[] databases,JedisClient client, CommandLine cl)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		int keyCount = cl.getArgc();
		int deletedCount = 0;
		for(int i = 0; i < keyCount;++i){
			if(Server.databases[client.getCurrntDB()].remove(cl.getArg(i))){
				++deletedCount;
			}
		}
		if(deletedCount < MessageConstant.NUMBER_COUNT){
			return MessageConstant.NUMBERS[deletedCount];
		}
		return new Sds(Integer.toString(deletedCount));
	}

}
