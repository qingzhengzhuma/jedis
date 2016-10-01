package jedis.util;

public class JedisEntry<K extends JedisObject,V extends JedisObject>{
	
	private K key;
	private V value;
	
	public JedisEntry(K key,V value){
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		// TODO Auto-generated method stub
		return this.key;
	}

	public V getValue() {
		// TODO Auto-generated method stub
		return this.value;
	}

	public V setValue(V value) {
		// TODO Auto-generated method stub
		V result = this.value;
		this.value = value;
		return result;
	}

}
