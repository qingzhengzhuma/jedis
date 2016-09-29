package jedis.util;

public class SelectHandler implements CommandHandler {
	
	@Override
	public JedisObject execute(JedisDB[]database,JedisClient client,CommandLine cl) 
			throws UnsupportedOperationException,IllegalArgumentException{
		
		try{
			int dbIndex = Integer.parseInt(cl.getArg(0));
			if(database == null || database.length == 0 || 
					dbIndex < 0 || dbIndex >= database.length){
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
