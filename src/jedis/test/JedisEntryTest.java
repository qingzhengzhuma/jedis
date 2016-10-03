package jedis.test;

import static org.junit.Assert.*;

import org.junit.Test;

import jedis.util.JedisEntry;
import jedis.util.JedisObject;
import jedis.util.Sds;

public class JedisEntryTest {

	@Test
	public void testAll() {
		Sds key = new Sds("msg");
		JedisObject value = new Sds("Hello World");
		Sds key1 = new Sds("another msg");
		JedisObject value1 = new Sds("Another world");
		JedisEntry<Sds, JedisObject> entry = new JedisEntry<>(key, value,key.hashCode()),
				entry2 = new JedisEntry<Sds, JedisObject>(key1, value1,key.hashCode());
		assertEquals(key.toString(), entry.getKey().toString());
		assertEquals(value.toString(), entry.getValue().toString());
		assertEquals(key1, entry2.getKey());
		assertEquals(value1, entry2.getValue());
	}

}
