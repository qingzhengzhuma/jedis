package jedis.util;

public class JedisHashTable<K extends JedisObject, V extends JedisObject> {
	JedisEntry<K, V>[] table;
	static final int DEFAULT_LENGTH = 4; 
	final double loadFactor = 0.75;
	int size = 16;
	int sizeMask;
	int used;
	int threshold;
	
	void clear(){
		table = null;
		size = 0;
		sizeMask = 0;
		used = 0;
		threshold = (int)loadFactor * size;
	}
	
	final int hash(K key) {
        return (key == null) ? 0 : key.hashCode();
    }
	
	public JedisHashTable() {
		this(DEFAULT_LENGTH);
	}
	
	@SuppressWarnings("unchecked")
	public JedisHashTable(int size) {
		table = (JedisEntry<K, V>[])new JedisEntry[size];
		this.size = size;
		this.used = 0;
		this.sizeMask = size - 1;
		this.threshold = (int)loadFactor * size;
	}
	
	
	V add(JedisEntry<K, V> entry){
		V oldValue = null;
		if(entry != null){
			int hash = entry.getHash() & sizeMask;
			if(table[hash] == null) table[hash] = entry;
			else{
				entry.next = table[hash];
				table[hash] = entry;
			}
			++used;
		}
		return oldValue;
	}

	public double factor() {
		return (double) used / (double) size;
	}

	public V get(K key) {
		int hash = hash(key) & sizeMask;
		JedisEntry<K, V> entry = table[hash];
		while(entry != null){
			if(entry.getKey().equals(key)) return entry.getValue();
			entry = entry.next;
		}
		return null;
	}
	
	//TODO: this can be optimized by user self-defined double-linked list 
	//now it's O(2n) complexity
	public V remove(K key) {
		int hash = hash(key) & sizeMask;
		JedisEntry<K, V> entry = table[hash];
		if(entry == null) return null;
		if(entry.getKey().equals(key)){
			table[hash] = entry.next;
			entry.next = null;
			--used;
			return entry.getValue();
		}
		JedisEntry<K, V> p = entry;
		entry = entry.next;
		while(entry != null){
			if(entry.getKey().equals(key)){
				p.next = entry.next;
				entry.next = null;
				--used;
				return entry.getValue();
			}
			entry = entry.next;
		}
		return null;
	}

	public V put(K key, V value) {
		int keyHash = hash(key);
		int hash = keyHash & sizeMask;
		if(table[hash] == null) {
			table[hash] = new JedisEntry<K,V>(key, value, keyHash);
			++used;
			return null;
		}
		
		JedisEntry<K, V> entry = table[hash];
		while(entry != null){
			if(entry.getKey().equals(key)){
				V val = entry.getValue();
				entry.setValue(value);
				return val;
			}
			entry = entry.next;
		}
		entry = new JedisEntry<K,V>(key, value, keyHash);
		entry.next = table[hash];
		table[hash] = entry;
		++used;
		return null;
	}

	public boolean containsKey(K key) {
		int hash = hash(key) & sizeMask;
		JedisEntry<K, V> entry = table[hash];
		while(entry != null){
			if(entry.getKey().equals(key)) return true;
			entry = entry.next;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public JedisHashTable<K, V> copy(){
		JedisHashTable<K, V> ht = new JedisHashTable<>(size);
		for(int i = 0; i < size;++i){
			JedisEntry<K, V> entry = table[i];
			if(entry != null){
				ht.table[i] = new JedisEntry<K,V>((K)entry.getKey().deepCopy(),
						(V)entry.getValue().deepCopy(), entry.getHash());
				entry = entry.next;
				JedisEntry<K, V> p = ht.table[i];
				while(entry != null){
					p.next = new JedisEntry<K,V>((K)entry.getKey().deepCopy(),
							(V)entry.getValue().deepCopy(), entry.getHash());
					p = p.next;
					entry = entry.next;
				}
			}
		}
		ht.used = this.used;
		return ht;
	}
}
