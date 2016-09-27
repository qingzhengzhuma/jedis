package jedis.util;

import java.util.HashMap;
import java.util.Map;

public class JedisDB {
	private Map<String, JedisObject> dict;
	
	public JedisDB(){
		dict = new HashMap<>();
	}
	
	public boolean containsKey(String key){
		return dict.containsKey(key);
	}
	
	public JedisObject get(String key){
		return dict.get(key);
	}
	
	public void set(String key,JedisObject value){
		dict.put(key, value);
	}
}
