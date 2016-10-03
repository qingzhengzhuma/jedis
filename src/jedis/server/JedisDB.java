package jedis.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import jedis.util.JedisObject;
import jedis.util.Sds;

public class JedisDB{
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
	
	public boolean remove(Sds key) {
		return dict.remove(key) != null;
	}
	
	Map<Sds, JedisObject> getDict(){
		return dict;
	}
	
	public JedisDB copy(){
		JedisDB db = new JedisDB();
		for(Entry<Sds, JedisObject> entry : dict.entrySet()){
			Sds key = entry.getKey().deepCopy();
			JedisObject value = entry.getValue().deepCopy();
			db.set(key, value);
		}
		return db;
	}
}
