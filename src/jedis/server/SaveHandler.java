package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisClient;
import jedis.util.JedisConfigration;
import jedis.util.JedisObject;
import jedis.util.MessageConstant;

public class SaveHandler implements CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		
		cRDB.save(Server.databases, JedisConfigration.rdbPathName);
		return MessageConstant.OK;
	}

}
