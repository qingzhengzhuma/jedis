package jedis.util;

/**
 * this map is used in jedis, it is not compatible with the jdk Map interface,
 * howerver, in order to support more speeding deep copy of a map, each key and
 * each value should implement a deepCopy method
 * 
 * @author liaojian
 *
 * @param <K>
 * @param <V>
 */
public class JedisMap<K extends JedisObject, V extends JedisObject> {
	private static int DICT_HT_INITIAL_SIZE = 4;
	private static final int MAX_SIZE = 1 << 30;
	private JedisHashTable<K, V> ht1;
	private JedisHashTable<K, V> ht2;
	private int rehashIdx;

	public JedisMap() {
		// TODO Auto-generated constructor stub
		ht1 = new JedisHashTable<>();
		ht2 = null;
		rehashIdx = -1;
	}

	private boolean isRehashing() {
		return rehashIdx != -1;
	}

	private void resize() {
		if (!isRehashing()) {
			int minimal = ht1.size;
			if (minimal < DICT_HT_INITIAL_SIZE)
				minimal = DICT_HT_INITIAL_SIZE;
			expand(minimal);
		}
	}

	private int getSizeFor(int size) {
		int i = DICT_HT_INITIAL_SIZE;

		if (size >= MAX_SIZE)
			return MAX_SIZE;
		while (i < size) {
			i <<= 1;
		}
		return i;
	}

	private void expand(int size) {
		int realSize = getSizeFor(size);
		if (!isRehashing() && ht1.used <= size && ht1.size != realSize) {
			ht2 = new JedisHashTable<>(realSize);
			if (ht1.table == null) {
				ht1 = ht2;
			} else {
				rehashIdx = 0;
			}
		}
	}

	private boolean rehash(int step) {
		int maxBuckets = step * 10;
		if (isRehashing()) {
			while (maxBuckets > 0 && ht1.used > 0 && ht1.size > rehashIdx) {
				if (ht1.table[rehashIdx] != null) {
					JedisEntry<K, V> e = ht1.table[rehashIdx];
					while (e != null) {
						JedisEntry<K, V> next = e.next;
						e.next = null;
						ht2.add(e);
						--ht1.used;
						e = next;
					}
					ht1.table[rehashIdx] = null;
				}
				++rehashIdx;
				--maxBuckets;
			}
			if (ht1.used == 0){
				ht1 = ht2;
				ht2 = null;
				rehashIdx = -1;
				return false;
			}
			return true;
		}
		return false;
	}

	public V get(K key) {
		rehash(10);
		V value = null;
		if(isRehashing() && (value = ht2.get(key))!= null){
			return value;
		}
		return ht1.get(key);
	}

	public V put(K key, V value) {
		rehash(10);
		V oldVal = null;
		if(isRehashing()){
			oldVal = ht2.put(key, value);
			if(oldVal == null){
				oldVal = ht1.remove(key);
			}
		}else{
			oldVal = ht1.put(key, value);
			if(ht1.used > ht1.size * ht1.loadFactor){
				resize();
			}
		}
		return oldVal;
	}

	public boolean containsKey(K key) {
		rehash(10);
		return (isRehashing() && ht2.containsKey(key)) || ht1.containsKey(key);
	}

	public V remove(K key) {
		rehash(10);
		V value = null;
		if(isRehashing() && (value = ht2.remove(key)) != null){
			return value;
		}
		return ht1.remove(key);
	}

	/**
	 * get a deep copy of the map
	 * 
	 * @return
	 */
	public JedisMap<K, V> copy() {
		while(isRehashing()){
			rehash(10);
		}
		JedisMap<K, V> newMap = new JedisMap<>();
		newMap.ht1 = ht1.copy();
		return newMap;
	}

	public int entrySize() {
		return ht1.used + (isRehashing() ? ht2.used : 0);
	}
}
