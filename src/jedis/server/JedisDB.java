package jedis.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jedis.util.JedisObject;
import jedis.util.Sds;

public class JedisDB{
	int id;
	Map<Sds, JedisObject> dict;
	Map<Sds, List<JedisClient>> watchedKeys;
	Map<Sds, Long> expireKeys;
	
	public JedisDB(){
		dict = new HashMap<>();
		watchedKeys = new HashMap<>();
		expireKeys = new HashMap<>();
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
	
	public boolean isKeyExpired(Sds key){
		Long expireTime = null;
		if((expireTime = expireKeys.get(key)) != null &&
				System.currentTimeMillis() >= expireTime){
			return true;
		}
		return false;
	}
	
	public void removeIfExpired(Sds key){
		if(isKeyExpired(key)){
			removeExpiredKey(key);
		}
	}
	
	public void removeExpiredKey(Sds key){
		expireKeys.remove(key);
		dict.remove(key);
	}
}
