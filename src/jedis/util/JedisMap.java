package jedis.util;

import java.util.Iterator;
import java.util.List;

/**
 * this map is used in jedis, it is not compatible with the jdk
 * Map interface, howerver, in order to support more speeding deep
 * copy of a map, each key and each value should implement a deepCopy
 * method
 * @author liaojian
 *
 * @param <K>
 * @param <V>
 */
public class JedisMap<K extends JedisObject,V extends JedisObject>{

	private JedisHashTable<K, V> ht1;
	private JedisHashTable<K, V> ht2;
	public JedisMap() {
		// TODO Auto-generated constructor stub
		ht1 = new JedisHashTable<>();
		ht2 = null;
	}
	
	public V get(K key){
		V value = ht1.get(key);
		if(value == null && ht2 != null) value = ht2.get(key);
		return value;
	}
	
	public V put(K key,V value){
		resize();
		if(ht2 != null){
			V oldValue = ht1.remove(key);
			V old1 = ht2.put(key, value);
			if(oldValue == null) oldValue = old1;
			return oldValue;
		}
		return ht1.put(key, value);
	}
	
	public boolean containsKey(K key){
		return ht1.containsKey(key) || (ht2 != null && ht2.containsKey(key));
	}
	
	public V remove(K key){
		V value = null;
		value = ht1.remove(key);
		V v1 = ht2 == null ? null : ht2.remove(key);
		if(value == null) value = v1;
		return value;
	}
	
	/**
	 * get a deep copy of the map
	 * @return
	 */
	public JedisMap<K, V> copy(){
		JedisMap<K, V> newMap = new JedisMap<>();
		newMap.ht1 = ht1.copy();
		newMap.ht2 = ht2 == null ? null : ht2.copy();
		return newMap;
	}
	
	public void resize(){
		if(ht1.getLength() <= 16) return;
		int newSize = 0;
		if(ht1.factor() > 0.75){
			if(ht1.getLength() == Integer.MAX_VALUE) return;
			newSize = ht1.getLength() * 2;
		}else if(ht1.factor() < 0.1){
			newSize = 16;
			while(newSize <= ht1.getUsed()){
				newSize <<= 1;
			}
			newSize = Math.max(newSize, 16);
		}
		ht2 = new JedisHashTable<>(newSize);
		Iterator<JedisEntry<K, V>> iterator =  ht1.iterator();
		while(iterator.hasNext()){
			ht2.add(iterator.next());
		}
		ht1 = ht2;
		ht2 = null;
	}
	
	public void rehash(){
		
	}
	
	
	public Iterator<JedisEntry<K, V>> iterator(){
		return ht1.iterator();
	}
	
	public int entrySize(){
		return ht1.getUsed();
	}
}
