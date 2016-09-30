package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisObject;
import jedis.util.MessageConstant;

public class SelectHandler implements CommandHandler {
	
	@Override
	public JedisObject execute(JedisClient client,CommandLine cl) 
			throws UnsupportedOperationException,IllegalArgumentException{
		
		try{
			int dbIndex = Integer.parseInt(cl.getArg(0));
			
			JedisDB[] databases = Server.inUseDatabases;
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
