package jedis.util;

import org.junit.internal.builders.NullBuilder;

import jedis.util.SetHandler.Entry;

public class GetHandler implements CommandHandler {
	
	private String parseKey(String command){
		String key = command.split(" ")[1];
		return key;
	}

	@Override
	public JedisObject execute(JedisDB[] databases, JedisClient client, String command) {
		// TODO Auto-generated method stub
		JedisDB db = databases[client.currentDB];
		String key = parseKey(command);
		if(db.containsKey(key)){
			JedisObject object = db.get(key);
			if(!(object instanceof Sds)) throw new UnsupportedOperationException();
			else return (Sds)object;
		}
		return null;
	}

}
