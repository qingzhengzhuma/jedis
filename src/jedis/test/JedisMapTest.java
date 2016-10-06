package jedis.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import jedis.util.JedisMap;
import jedis.util.JedisObject;
import jedis.util.Sds;

public class JedisMapTest {
	
	int testCount = 10000;

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
	
	@Test
	public void testJedisMap() {
		JedisMap<Sds, JedisObject> map = new JedisMap<>();
		
		for(int i = 1; i <= testCount;++i){
			map.put(new Sds(Integer.toString(i)), new Sds(Integer.toString(i+1)));
		}
		
		HashMap<Sds, JedisObject> jdkmap = new HashMap<>();
		for(int i = 1; i <= testCount;++i){
			jdkmap.put(new Sds(Integer.toString(i)), new Sds(Integer.toString(i+1)));
		}
		
		for(int i = 1; i <= testCount;++i){
			assertEquals(map.get(new Sds(Integer.toString(i))), jdkmap.get(new Sds(Integer.toString(i))));
		}
	}
	
	@Test
	public void testJedisMapPerformance() {
		JedisMap<Sds, JedisObject> map = new JedisMap<>();
		
		for(int i = 1; i <= testCount;++i){
			map.put(new Sds(Integer.toString(i)), new Sds(Integer.toString(i+1)));
		}
	}
	
	@Test
	public void testJdkMapPerformance() {
		HashMap<Sds, JedisObject> jmap = new HashMap<>();
		for(int i = 1; i <= testCount;++i){
			jmap.put(new Sds(Integer.toString(i)), new Sds(Integer.toString(i+1)));
		}
	}

}
