package jedis.test;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import jedis.util.JedisEntry;
import jedis.util.JedisHashTable;
import jedis.util.JedisObject;
import jedis.util.Sds;

public class JedisHashTableTest {

	
	@Test
	public void testAdd() {
		JedisHashTable<Sds, JedisObject> hashTable = new JedisHashTable<>();
		Sds key = new Sds("msg");
		JedisObject value = new Sds("Hello World");
		JedisObject value1 = new Sds("Another world");
		JedisEntry<Sds, JedisObject> entry = new JedisEntry<Sds, JedisObject>(key, value);
		JedisEntry<Sds, JedisObject> entry1 = new JedisEntry<Sds, JedisObject>(key, value1);
		assertEquals(null,hashTable.add(entry));
		assertEquals(value, hashTable.add(entry1));
		assertEquals(value1, hashTable.get(key));
		assertEquals(null, hashTable.add(null));
		assertEquals(0.0625, hashTable.factor(),1e-15);
	}
	
	@Test
	public void testFactor() {
		JedisHashTable<Sds, JedisObject> hashTable = new JedisHashTable<>();
		assertEquals(0.0, hashTable.factor(),1e-15);
		Sds key = new Sds("msg");
		JedisObject value = new Sds("Hello World");
		hashTable.put(key, value);
		assertEquals(1.0 / 16.0, hashTable.factor(),1e-15);
	}

	@Test
	public void testGet() {
		JedisHashTable<Sds, JedisObject> hashTable = new JedisHashTable<>();
		Sds key = new Sds("msg");
		JedisObject value = new Sds("Hello World");
		assertEquals(null,hashTable.get(key));
		hashTable.put(key, value);
		assertEquals(value.toString(),hashTable.get(key).toString());
		assertEquals(value, hashTable.get(key));
	}

	@Test
	public void testRemove() {
		JedisHashTable<Sds, JedisObject> hashTable = new JedisHashTable<>();
		Sds key = new Sds("msg");
		JedisObject value = new Sds("Hello World");
		Sds key1 = new Sds("another msg");
		JedisObject value1 = new Sds("Another world");
		hashTable.put(key, value);
		assertEquals(value.toString(),hashTable.get(key).toString());
		assertEquals(value, hashTable.get(key));
		hashTable.put(key1, value1);
		assertEquals(0.125, hashTable.factor(),1e-15);
		assertEquals(value1, hashTable.get(key1));
		assertEquals(value, hashTable.remove(key));
		assertEquals(null, hashTable.get(key));
		assertEquals(value1, hashTable.get(key1));
		assertEquals(0.0625, hashTable.factor(),1e-15);
	}

	@Test
	public void testPut() {
		JedisHashTable<Sds, JedisObject> hashTable = new JedisHashTable<>();
		Sds key = new Sds("msg");
		JedisObject value = new Sds("Hello World");
		JedisObject value1 = new Sds("Another world");
		assertEquals(null,hashTable.put(key, value));
		assertEquals(value, hashTable.put(key, value1));
		assertEquals(value1, hashTable.get(key));
	}

	@Test
	public void testContainsKey() {
		JedisHashTable<Sds, JedisObject> hashTable = new JedisHashTable<>();
		Sds key = new Sds("msg");
		JedisObject value = new Sds("Hello World");
		Sds key1 = new Sds("Another world");
		hashTable.put(key, value);
		assertEquals(true, hashTable.containsKey(key));
		assertEquals(false, hashTable.containsKey(key1));
	}
	
	@Test
	public void testIterator(){
		Sds key = new Sds("msg");
		JedisObject value = new Sds("Hello World");
		Sds key1 = new Sds("Another world");
		JedisObject value1 = new Sds("Another world");
		JedisHashTable<Sds, JedisObject> ht = new JedisHashTable<>();
		Iterator<JedisEntry<Sds, JedisObject>> iterator = ht.iterator();
		assertEquals(false, iterator.hasNext());
		ht.put(key, value);
		iterator = ht.iterator();
		assertEquals(true, iterator.hasNext());
		JedisEntry<Sds, JedisObject> entry = iterator.next();
		assertEquals(key,entry.getKey());
		assertEquals(value, entry.getValue());
		assertEquals(false, iterator.hasNext());
		ht.put(key1, value1);
		iterator = ht.iterator();
		assertEquals(true, iterator.hasNext());
		iterator.next();
		assertEquals(true, iterator.hasNext());
		iterator.next();
		assertEquals(false, iterator.hasNext());
	}

}
