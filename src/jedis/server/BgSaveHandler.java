package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisObject;
import jedis.util.MessageConstant;

public class BgSaveHandler implements CommandHandler{

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		if(Server.aofSaveThread.isAlive()) return MessageConstant.AOF_BUSY;
		int dbNum = Server.inUseDatabases.length;
		//Server.bufDatabases = Server.initDatabases(dbNum);
		//Server.deletedDatabases = Server.initDatabases(dbNum);
		JedisDB[] dbs = Server.initDatabases(dbNum);
		for(int i = 0; i < dbNum;++i){
			dbs[i] = Server.inUseDatabases[i].copy();
		}
		Server.rdbSaveThread = new RdbSaveThread(dbs);
		Server.rdbSaveThread.start();
		return MessageConstant.OK;
	}

}
