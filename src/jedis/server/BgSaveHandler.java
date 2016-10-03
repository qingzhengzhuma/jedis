package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisObject;

public class BgSaveHandler implements CommandHandler{

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl) 
			throws UnsupportedOperationException {
		if(Server.aofSaveThread.isAlive()) return MessageConstant.AOF_BUSY;
		int dbNum = Server.inUseDatabases.length;
		JedisDB[] dbs = new JedisDB[dbNum];
		for(int i = 0; i < dbNum;++i){
			dbs[i] = Server.inUseDatabases[i].copy();
		}
		Server.rdbSaveThread = new RdbSaveThread(dbs);
		Server.rdbSaveThread.start();
		return MessageConstant.OK;
	}

}
