package jedis.server;

import jedis.util.JedisMap;
import jedis.util.JedisObject;
import jedis.util.Sds;

public class JedisDB{
	private JedisMap<Sds, JedisObject> dict;
	
	public JedisDB(){
		dict = new JedisMap<>();
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
	
	JedisMap<Sds, JedisObject> getDict(){
		return dict;
	}
	
	public JedisDB copy(){
		JedisDB db = new JedisDB();
		db.dict = dict.copy();
		return db;
	}
}
