package jedis.util;

import jedis.server.Server;

public class SaveHandler implements CommandHandler {

	@Override
	public JedisObject execute(JedisDB[] databases, JedisClient client, CommandLine cl)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		Server.rdbSaveThread = new RdbSaveThread(databases);
		Server.rdbSaveThread.start();
		return MessageConstant.OK;
	}

}
