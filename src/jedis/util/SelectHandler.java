package jedis.util;

public class SelectHandler implements CommandHandler {
	private int parseDBNum(String command) {
		// TODO Auto-generated method stub
		return 1;
	}
	
	@Override
	public JedisObject execute(JedisDB[]database,JedisClient client,String command) {
		int dbNum = parseDBNum(command);
		if(database == null || database.length == 0 || 
				dbNum < 0 || dbNum >= database.length){
			return new Sds("ERROR");
		}
		client.setCurrentDB(parseDBNum(command));
		return new Sds("OK");
	}

}
