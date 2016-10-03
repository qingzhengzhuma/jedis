package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisObject;

public class SaveHandler implements CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		RDB.save(Server.inUseDatabases, JedisConfigration.rdbPathName);
		return MessageConstant.OK;
	}

}
