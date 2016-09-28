package jedis.util;

public class SelectHandler implements CommandHandler {
	private int parseDBNum(String command) {
		// TODO Auto-generated method stub
		String[] args = command.split(" ");
		try{
			int dbNum = Integer.parseInt(args[1]);
			return dbNum;
		}catch (NumberFormatException e) {
			// TODO: handle exception
			throw new IllegalArgumentException();
		}
		
	}
	
	@Override
	public JedisObject execute(JedisDB[]database,JedisClient client,String command) 
			throws UnsupportedOperationException,IllegalArgumentException{
		int dbNum = parseDBNum(command);
		if(database == null || database.length == 0 || 
				dbNum < 0 || dbNum >= database.length){
			return new Sds("ERROR");
		}
		client.setCurrentDB(parseDBNum(command));
		return new Sds("OK");
	}

}
