package jedis.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jedis.util.Sds;

public class SdsTest {
	private static String[] strings = {"abc","defg",""};
	private Sds[] sdss = new Sds[strings.length];
	
	@Before
	public void setUp(){
		for(int i = 0; i < strings.length;++i){
			sdss[i] = new Sds(strings[i]);
		}
	}
	
	@After
	public void tearDown(){
		System.out.println("tearDown");
	}
	
	@Test
	public void testEqualsObject() {
		assertEquals(sdss[0] == sdss[1], false);
		assertEquals(sdss[0], sdss[0]);
		assertEquals(sdss[0], sdss[0]);
		assertEquals(sdss[2], sdss[2]);
		assertNotEquals(sdss[0], sdss[2]);
	}

	@Test
	public void testToString() {
		assertEquals(strings[0], sdss[0].toString());
		assertEquals("abc", sdss[0].toString());
		assertEquals("", sdss[2].toString());
	}

	@Test
	public void testCopyFromCharArrayInt() {
		sdss[0].copyFrom(strings[2].getBytes(), 0);
		assertEquals(strings[2], sdss[0].toString());
		sdss[0].copyFrom(strings[0].getBytes(),0);
		assertEquals(strings[2], sdss[0].toString());
		sdss[0].copyFrom(strings[0].getBytes(),2);
		assertEquals(strings[0].substring(0, 2), sdss[0].toString());
		sdss[0].copyFrom(strings[0].getBytes(),strings[0].length());
		assertEquals(strings[0], sdss[0].toString());
	}

	@Test
	public void testAppendCharArrayInt() {
		sdss[0].append(strings[2].getBytes(), 0);
		assertEquals(strings[0], sdss[0].toString());
		sdss[0].append(strings[0].getBytes(),0);
		assertEquals(strings[0], sdss[0].toString());
		sdss[0].append(strings[0].getBytes(),2);
		assertEquals(strings[0] + strings[0].substring(0, 2), sdss[0].toString());
		sdss[0].append(strings[0].getBytes(),strings[0].length());
		assertEquals(strings[0] + strings[0].substring(0, 2) + strings[0], sdss[0].toString());
	}
	
	@Test
	public void testClone(){
		Sds sds = sdss[0].clone();
		assertEquals(sds, sdss[0]);
		assertEquals(sds == sdss[0], false);
	}
	
	@Test
	public void testWorkWithMap(){
		Map<Sds,Integer> map = new HashMap<>();
		int i = 0;
		for(Sds sds : sdss){
			map.put(sds, i++);
		}
		for(int j = 0; j < sdss.length;++j){
			assertEquals(j, (int)map.get(sdss[j]));
		}
		assertEquals(false, map.containsKey(new Sds("hello")));
		assertEquals(true, map.containsKey(new Sds("abc")));
 	}
	
	@Test
	public void testResize(){
		assertEquals(3,sdss[0].resize(0));
		assertEquals(3,sdss[0].resize(2));
		assertEquals(3,sdss[0].resize(3));
		assertEquals(60,sdss[0].resize(60));
		assertEquals(37,sdss[0].resize(37));
	}

}
