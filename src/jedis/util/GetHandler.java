package jedis.util;

public class GetHandler implements CommandHandler {
	
	private String parseKey(String command){
		String key = command.split("\\s")[1];
		return key;
	}

	@Override
	public JedisObject execute(JedisDB[] databases, JedisClient client, String command) {
		// TODO Auto-generated method stub
		String key = parseKey(command);
		JedisDB db = databases[client.currentDB];
		return db.get(key);
	}

}
