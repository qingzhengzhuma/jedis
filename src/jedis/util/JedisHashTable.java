package jedis.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class JedisHashTable<K extends JedisObject, V extends JedisObject> {
	private List<JedisEntry<K, V>>[] table;
	private static final int DEFAULT_LENGTH = 16; 
	private static final int MAX_LENGTH = 1<<30;
	private final double loadFactor = 0.75;
	private int length = 16;
	private int lengthMask;
	private int used;
	private int threshold;
	
	final int hash(K key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
	
	final int getSizeFor(int capacity){
		if(capacity <= DEFAULT_LENGTH) return DEFAULT_LENGTH;
		int size = MAX_LENGTH;
		while(size>>1 >= capacity){
			size >>= 1;
		}
		return size;
	}
	
	public JedisHashTable() {
		// TODO Auto-generated constructor stub
		this(DEFAULT_LENGTH);
	}
	
	@SuppressWarnings({"unchecked"})
	public JedisHashTable(int capacity) {
		// TODO Auto-generated constructor stub
		length = getSizeFor(capacity);
		table = (List<JedisEntry<K, V>>[]) new LinkedList[length];
		lengthMask = length - 1;
		threshold = (int)loadFactor * length;
		used = 0;
	}
	
	public int getLength(){
		return length;
	}
	
	public int getUsed() {
		return used;
	}
	
	V add(JedisEntry<K, V> entry){
		V oldValue = null;
		if(entry != null){
			int hash = entry.getHash() & lengthMask;
			if(table[hash] == null) table[hash] = new LinkedList<>();
			table[hash].add(entry);
			++used;
		}
		return oldValue;
	}

	public double factor() {
		return (double) used / (double) length;
	}

	public V get(K key) {
		int hash = hash(key) & lengthMask;
		List<JedisEntry<K, V>> candidates = table[hash];
		if(candidates != null){
			for(JedisEntry<K, V> entry : candidates){
				if(entry.getKey().equals(key)) return entry.getValue();
			}
		}
		return null;
	}
	
	//TODO: this can be optimized by user self-defined double-linked list 
	//now it's O(2n) complexity
	public V remove(K key) {
		int hash = hash(key) & lengthMask;
		List<JedisEntry<K, V>> candidates = table[hash];
		if(candidates == null) return null;
		int index = -1,i = 0;
		for(JedisEntry<K, V> entry : candidates){
			if(entry.getKey().equals(key)){
				index = i;
				break;
			}
			++i;
		}
		V value = null;
		if(index >= 0 && index < candidates.size()){
			value = candidates.remove(index).getValue();
			--used;
		}
		return value;
	}

	public V put(K key, V value) {
		int keyHash = hash(key);
		int hash = keyHash & lengthMask;
		if(table[hash] == null) table[hash] = new LinkedList<>();
		List<JedisEntry<K, V>> candidates = table[hash];
		V oldValue = null;
		boolean isContain = false;
		for(JedisEntry<K, V> entry : candidates){
			if(entry.getKey().equals(key)){
				oldValue = entry.getValue();
				entry.setValue(value);
				isContain = true;
				break;
			}
		}
		if(!isContain){
			candidates.add(0,new JedisEntry<K,V>(key,value,keyHash));
			if(++used > threshold);
				//resize();
		}
		return oldValue;
	}
	
	void resize(){
		if(length == MAX_LENGTH) return;
		int newSize = length * 2;
		JedisHashTable<K, V> ht2 = new JedisHashTable<>(newSize);
		Iterator<JedisEntry<K, V>> iterator =  iterator();
		while(iterator.hasNext()){
			ht2.add(iterator.next());
		}
		this.table = ht2.table;
		this.used = ht2.used;
		this.length = ht2.length;
		this.lengthMask = this.length - 1;
		this.threshold = ht2.threshold;
		ht2 = null;
	}

	public boolean containsKey(K key) {
		int hash = hash(key) & lengthMask;
		List<JedisEntry<K, V>> candidates = table[hash];
		if(candidates == null) return false;
		for(JedisEntry<K, V> entry : candidates){
			if(entry.getKey().equals(key)){
				return true;
			}
		}
		return false;
	}
	
	public JedisHashTable<K, V> copy(){
		JedisHashTable<K, V> ht = new JedisHashTable<>(length);
		for(int i = 0; i < length;++i){
			List<JedisEntry<K, V>> candidates = table[i];
			for(JedisEntry<K, V> entry : candidates){
				@SuppressWarnings("unchecked")
				K key = (K)entry.getKey().deepCopy();
				@SuppressWarnings("unchecked")
				V value = (V)entry.getValue().deepCopy();
				int hash = entry.getHash();
				ht.table[i].add(0,new JedisEntry<K, V>(key, value,hash));
			}
		}
		ht.used = this.used;
		ht.lengthMask = this.lengthMask;
		return ht;
	}
	
	public Iterator<JedisEntry<K, V>> iterator(){
		return new Iterator<JedisEntry<K,V>>() {
			int i = 0;
			Iterator<JedisEntry<K, V>> iterator = null;
			{
				while(i < length && table[i] == null){
					++i;
				}
				if(i < length) iterator = table[i].iterator();
			}
			
			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				if(i < length && iterator.hasNext()) return true;
				while(++i < length && 
						(table[i] == null || 
						!table[i].iterator().hasNext()));
				if(i < length) iterator = table[i].iterator();
				return i < length && iterator.hasNext();
			}

			@Override
			public JedisEntry<K, V> next() {
				// TODO Auto-generated method stub
				return iterator.next();
			}
		};
	}
}
