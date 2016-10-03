package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisObject;
import jedis.util.MessageConstant;
import jedis.util.Sds;

public class TypeHandler implements CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		int curDB = client.getCurrntDB();
		Sds key = new Sds(cl.getArg(0));
		Server.removeIfExpired(key, curDB);
		JedisObject value = Server.inUseDatabases[curDB].get(key);
		if(value != null){
			return value.type();
		}
		return MessageConstant.NO_SUCH_KEY;
	}

}
