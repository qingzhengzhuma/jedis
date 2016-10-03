package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisObject;
import jedis.util.MessageConstant;
import jedis.util.Sds;

public class ExistsHandler implements CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		Sds key = new Sds(cl.getArg(0));
		int curDB = client.getCurrntDB();
		Server.removeIfExpired(key, curDB);
		if(Server.inUseDatabases[curDB].containsKey(key)){
			return MessageConstant.YES;
		}else{
			return MessageConstant.NO;
		}
	}

}
