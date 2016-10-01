package jedis.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class JedisHashTable<K extends JedisObject, V extends JedisObject> {
	private List<List<JedisEntry<K, V>>> table;
	private int length = 16;
	private int lengthMask;
	private int used;

	public JedisHashTable() {
		// TODO Auto-generated constructor stub
		table = new ArrayList<>(length);
		for(int i = 0; i < length;++i){
			table.add(new LinkedList<>());
		}
		lengthMask = length - 1;
	}
	
	public JedisHashTable(int capacity) {
		// TODO Auto-generated constructor stub
		if(capacity < 16) capacity = 16;
		this.length = capacity;
		table = new ArrayList<>(length);
		for(int i = 0; i < length;++i){
			table.add(new LinkedList<>());
		}
		lengthMask = length - 1;
	}
	
	public int getLength(){
		return length;
	}
	
	public int getUsed() {
		return used;
	}
	
	public List<JedisEntry<K, V>> getEntry(int index){
		return table.get(index);
	}
	
	public V add(JedisEntry<K, V> entry){
		V oldValue = null;
		if(entry != null){
			K key = entry.getKey();
			V value = entry.getValue();
			int hash = key.hashCode() & lengthMask;
			List<JedisEntry<K, V>> candidates = table.get(hash);
			boolean isContain = false;
			for(JedisEntry<K, V> e : candidates){
				if(e.getKey().equals(key)){
					oldValue = e.getValue();
					e.setValue(value);
					isContain = true;
					break;
				}
			}
			if(!isContain){
				candidates.add(0,entry);
				++used;
			}
		}
		return oldValue;
	}

	public double factor() {
		return (double) used / (double) length;
	}

	public V get(K key) {
		int hash = key.hashCode() & lengthMask;
		List<JedisEntry<K, V>> candidates = table.get(hash);
		for(JedisEntry<K, V> entry : candidates){
			if(entry.getKey().equals(key)) return entry.getValue();
		}
		return null;
	}
	
	//TODO: this can be optimized by user self-defined double-linked list 
	//now it's O(2n) complexity
	public V remove(K key) {
		int hash = key.hashCode() & lengthMask;
		List<JedisEntry<K, V>> candidates = table.get(hash);
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
		int hash = key.hashCode() & lengthMask;
		List<JedisEntry<K, V>> candidates = table.get(hash);
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
			candidates.add(0,new JedisEntry<K,V>(key,value));
			++used;
		}
		return oldValue;
	}

	public boolean containsKey(K key) {
		int hash = key.hashCode() & lengthMask;
		List<JedisEntry<K, V>> candidates = table.get(hash);
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
			List<JedisEntry<K, V>> candidates = table.get(i);
			for(JedisEntry<K, V> entry : candidates){
				K key = (K)entry.getKey().deepCopy();
				V value = (V)entry.getValue().deepCopy();
				ht.getEntry(i).add(0,new JedisEntry<K, V>(key, value));
			}
		}
		ht.used = this.used;
		ht.lengthMask = this.lengthMask;
		return ht;
	}
}
