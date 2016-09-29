package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisClient;
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
		//once bufDatabases != null, the key might appear in both
		//databases and data in bufDatabases is newer than that in
		//databases so check bufDatabases first and than databases
		if(Server.bufDatabases != null &&
		   Server.bufDatabases[curDB].containsKey(key) ||
			Server.databases[curDB].containsKey(key)){
			return MessageConstant.YES;
		}else{
			return MessageConstant.NO;
		}
	}

}
