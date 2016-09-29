package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisClient;
import jedis.util.JedisObject;
import jedis.util.MessageConstant;

public class BgSaveHandler implements CommandHandler{

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		if(Server.aofSaveThread.isAlive()) return MessageConstant.AOF_BUSY;
		Server.rdbSaveThread = new RdbSaveThread(Server.databases);
		Server.rdbSaveThread.start();
		return MessageConstant.OK;
	}

}
