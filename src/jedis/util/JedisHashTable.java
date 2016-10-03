package jedis.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class JedisHashTable<K extends JedisObject, V extends JedisObject> {
	private List<JedisEntry<K, V>>[] table;
	private static final int DEFAULT_LENGTH = 16; 
	private static final int MAX_LENGTH = 1<<30;
	private int length = 16;
	private int lengthMask;
	private int used;
	private static double MAX_LOAD_FACTOR = 0.75;
	
	final int hash(K key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
	
	final int getSizeFor(int capacity){
		int size = MAX_LENGTH;
		while(size>>1 > capacity){
			size >>= 1;
		}
		return size;
	}
	
	public JedisHashTable() {
		// TODO Auto-generated constructor stub
		this(DEFAULT_LENGTH);
	}
	
	@SuppressWarnings({"rawtypes","unchecked"})
	public JedisHashTable(int capacity) {
		// TODO Auto-generated constructor stub
		if(capacity < DEFAULT_LENGTH) capacity = DEFAULT_LENGTH;
		this.length = getSizeFor(capacity);
		table = (List<JedisEntry<K, V>>[]) new LinkedList[length];
		lengthMask = length - 1;
	}
	
	public int getLength(){
		return length;
	}
	
	public int getUsed() {
		return used;
	}
	
	public V add(JedisEntry<K, V> entry){
		V oldValue = null;
		if(entry != null){
			K key = entry.getKey();
			V value = entry.getValue();
			int hash = entry.getHash() & lengthMask;
			List<JedisEntry<K, V>> candidates = table[hash];
			boolean isContain = false;
			if(candidates == null){
				table[hash] = new LinkedList<>();
			}else{
				for(JedisEntry<K, V> e : candidates){
					if(e.getKey().equals(key)){
						oldValue = e.getValue();
						e.setValue(value);
						isContain = true;
						break;
					}
				}
			}
			if(!isContain){
				table[hash].add(entry);
				++used;
			}
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
			++used;
		}
		return oldValue;
	}

	public boolean containsKey(K key) {
		int hash = hash(key) & lengthMask;
		List<JedisEntry<K, V>> candidates = table[hash];
		boolean isContain = false;
		for(JedisEntry<K, V> entry : candidates){
			if(entry.getKey().equals(key)){
				isContain = true;
				break;
			}
		}
		return isContain;
	}
	
	public JedisHashTable<K, V> copy(){
		JedisHashTable<K, V> ht = new JedisHashTable<>(length);
		for(int i = 0; i < length;++i){
			List<JedisEntry<K, V>> candidates = table[i];
			for(JedisEntry<K, V> entry : candidates){
				K key = (K)entry.getKey().deepCopy();
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
			Iterator<JedisEntry<K, V>> iterator = table[0].iterator();
			
			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				while(!iterator.hasNext() && i < length){
					iterator = table[i].iterator();
				}
				return iterator.hasNext();
			}

			@Override
			public JedisEntry<K, V> next() {
				// TODO Auto-generated method stub
				return iterator.next();
			}
		};
	}
}
