package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisObject;
import jedis.util.Sds;

public class GetHandler implements CommandHandler {

	@Override
	public JedisObject execute( JedisClient client,CommandLine cl) {
		// TODO Auto-generated method stub
		int curDB = client.getCurrntDB();
		Sds key = new Sds(cl.getArg(0));
		Server.removeIfExpired(key, curDB);
		return Server.inUseDatabases[curDB].get(key);
	}

}
