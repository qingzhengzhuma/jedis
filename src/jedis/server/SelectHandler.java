package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisClient;
import jedis.util.JedisObject;
import jedis.util.MessageConstant;

public class SelectHandler implements CommandHandler {
	
	@Override
	public JedisObject execute(JedisClient client,CommandLine cl) 
			throws UnsupportedOperationException,IllegalArgumentException{
		
		try{
			int dbIndex = Integer.parseInt(cl.getArg(0));
			//once bufDatabase != null we should check bufDatabases for
			//data in bufaDatabases is newer than that in databases
			JedisDB[] databases = Server.bufDatabases != null ? Server.bufDatabases : Server.databases;
			if(databases == null || databases.length == 0 || 
					dbIndex < 0 || dbIndex >= databases.length){
				return MessageConstant.INDEX_OUT_OF_BOUNDARY;
			}
			client.setCurrentDB(dbIndex);
			return MessageConstant.OK;
		}catch (NumberFormatException e) {
			// TODO: handle exception
			throw new IllegalArgumentException();
		}
	}

}
