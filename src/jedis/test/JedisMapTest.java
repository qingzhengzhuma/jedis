package jedis.test;

import static org.junit.Assert.*;

import org.junit.Test;

import jedis.util.JedisMap;
import jedis.util.JedisObject;
import jedis.util.Sds;

public class JedisMapTest {

	@Test
	public void testGet() {
		JedisMap<Sds, JedisObject> map = new JedisMap<>();
		Sds key = new Sds("msg");
		JedisObject value = new Sds("Hello World");
		Sds key1 = new Sds("another msg");
		JedisObject value1 = new Sds("Another world");
		assertEquals(null, map.get(key));
		map.put(key, value);
		assertEquals(value.toString(), map.get(key).toString());
		assertEquals(value, map.get(key));
		map.put(key1, value1);
		assertEquals(value, map.get(key));
		assertEquals(value1, map.get(key1));
	}

	@Test
	public void testPut() {
		JedisMap<Sds, JedisObject> map = new JedisMap<>();
		Sds key = new Sds("msg");
		JedisObject value = new Sds("Hello World");
		Sds key1 = new Sds("another msg");
		JedisObject value1 = new Sds("Another world");
		assertEquals(null, map.put(key, value));
		assertEquals(null,map.put(key1, value1));
		assertEquals(value, map.put(key, value1));
	}

	@Test
	public void testContainsKey() {
		JedisMap<Sds, JedisObject> map = new JedisMap<>();
		Sds key = new Sds("msg");
		JedisObject value = new Sds("Hello World");
		Sds key1 = new Sds("another msg");
		JedisObject value1 = new Sds("Another world");
		map.put(key, value);
		assertEquals(true, map.containsKey(key));
		assertEquals(false, map.containsKey(key1));
		map.put(key1, value1);
		assertEquals(true, map.containsKey(key1));
	}
	
	@Test
	public void testRemove(){
		JedisMap<Sds, JedisObject> map = new JedisMap<>();
		Sds key = new Sds("msg");
		JedisObject value = new Sds("Hello World");
		Sds key1 = new Sds("another msg");
		JedisObject value1 = new Sds("Another world");
		assertEquals(null, map.remove(key));
		map.put(key, value);
		assertEquals(value.toString(), map.remove(key).toString());
		assertEquals(null, map.remove(key));
		map.put(key1, value1);
		assertEquals(value1, map.remove(key1));
	}

}
