package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisClient;
import jedis.util.JedisObject;
import jedis.util.Sds;

public class GetHandler implements CommandHandler {

	@Override
	public JedisObject execute( JedisClient client,CommandLine cl) {
		// TODO Auto-generated method stub
		int curDB = client.getCurrntDB();
		Sds key = new Sds(cl.getArg(0));
		
		//once bufDataBases != null we know the rdb is saving
		//background therefore we must search both databases,
		//and because bufDatabases is always newer than databases,
		//so we search bufDatabases first
		if(Server.bufDatabases != null &&
		   Server.bufDatabases[curDB].containsKey(key)){
			return Server.bufDatabases[curDB].get(key);
		}
		if(Server.databases[curDB].containsKey(key)){
			return Server.databases[curDB].get(key);
		}
		return null;
	}

}
