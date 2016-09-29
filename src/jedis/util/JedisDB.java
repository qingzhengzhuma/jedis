package jedis.util;

import java.util.HashMap;
import java.util.Map;

public class JedisDB {
	private Map<Sds, JedisObject> dict;
	
	public JedisDB(){
		dict = new HashMap<>();
	}
	
	public boolean containsKey(Sds key){
		return dict.containsKey(key);
	}
	
	public JedisObject get(Sds key){
		return dict.get(key);
	}
	
	public void set(Sds key,JedisObject value){
		dict.put(key, value);
	}
	
	public boolean remove(String key) {
		return dict.remove(key) != null;
	}
	
	Map<Sds, JedisObject> getDict(){
		return dict;
	}
}
