package jedis.util;

import java.util.HashMap;
import java.util.Map;

public class JedisDB {
	private Map<String, JedisObject> dict;
	
	public JedisDB(){
		dict = new HashMap<>();
	}
}
