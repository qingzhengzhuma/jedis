package jedis.test;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import jedis.util.ZipList;

public class ZipListTest {

	@Test
	public void testGetEntrySize() {
		ZipList zipList = new ZipList();
		assertEquals(0, zipList.getEntrySize());
		char[] entry = null;
		zipList.push(entry);
		assertEquals(0, zipList.getEntrySize());
		entry = new char[0];
		assertEquals(0, zipList.getEntrySize());
		entry = "sgedhtgrherjrtyjtyukerthwerutykyujtgehrwujuytrjtyjherethrhrehello".toCharArray();
		zipList.push(entry);
		assertEquals(1, zipList.getEntrySize());
	}

	@Test
	public void testGetBlobSize() {
		ZipList zipList = new ZipList();
		assertEquals(0, zipList.getBlobSize());
		char[] entry = null;
		zipList.push(entry);
		assertEquals(0, zipList.getBlobSize());
		entry = new char[0];
		assertEquals(0, zipList.getBlobSize());
		entry = "sgedhtgrherjrtyjtyukerthwerutykyujtgehrwujuytrjtyjherethrhrehello".toCharArray();
		zipList.push(entry);
		assertEquals(66, zipList.getBlobSize());
	}
	
	@Test
	public void testPush(){
		ZipList zipList = new ZipList();
		String string = "sgedhtgrherjrtyjtyukerthwerutykyujtgehrwujuytrjtyjherethrhrehello";
		char[] entry = string.toCharArray();
		zipList.push(entry);
		String s1 = (char)(entry.length) + string;
		assertEquals(s1, zipList.toString());
		String string2 = "women";
		entry = string2.toCharArray();
		zipList.push(entry);
		String s2 = (char)(entry.length) + string2;
		assertEquals(s1+s2,zipList.toString());
	}
	
	@Test
	public void testInsertAfter(){
		ZipList zipList = new ZipList();
		String string = "sgedhtgrherjrtyjtyukerthwerutykyujtgehrwujuytrjtyjherethrhrehello";
		char[] entry = string.toCharArray();
		zipList.push(entry);
		String s1 = (char)(entry.length) + string;
		assertEquals(s1, zipList.toString());
		String string2 = "women";
		entry = string2.toCharArray();
		zipList.insert(entry, 0, true);
		String s2 = (char)(entry.length) + string2;
		assertEquals(s1 + s2,zipList.toString());
	}
	
	@Test
	public void testInsertBefore(){
		ZipList zipList = new ZipList();
		String string = "sgedhtgrherjrtyjtyukerthwerutykyujtgehrwujuytrjtyjherethrhrehello";
		char[] entry = string.toCharArray();
		zipList.push(entry);
		String s1 = (char)(entry.length) + string;
		assertEquals(s1, zipList.toString());
		String string2 = "women";
		entry = string2.toCharArray();
		zipList.insert(entry, 0, false);
		String s2 = (char)(entry.length) + string2;
		assertEquals(s2 + s1,zipList.toString());
	}
	
	@Test
	public  void testRemove() {
		ZipList zipList = new ZipList();
		String string = "sgedhtgrherjrtyjtyukerthwerutykyujtgehrwujuytrjtyjherethrhrehello";
		String string2 = "women";
		String string3 = "hello word";
		String[] strings = {string,string2,string3};
		for(String string4 : strings){
			zipList.push(string4.toCharArray());
		}
		assertEquals(string2, new String(zipList.get(1)));
		zipList.remove(1);
		assertEquals(string3, new String(zipList.get(1)));
	}
	
	@Test
	public void testRemoveRange(){
		ZipList list1 = new ZipList(),list3 = new ZipList();
		String[] strings = {"abc","def","higklmn","weth","wo"};
		int i = 0;
		for(;i < 3;++i){
			list1.push(strings[i].toCharArray());
			list3.push(strings[i].toCharArray());
		}
		for(;i < 5; ++i){
			list3.push(strings[i].toCharArray());
		}
		assertNotEquals(list3, list1);
		list3.removeRange(3,2);
		assertEquals(list3, list1);
	}
	
	@Test
	public void testGet(){
		ZipList zipList = new ZipList();
		String string = "sgedhtgrherjrtyjtyukerthwerutykyujtgehrwujuytrjtyjherethrhrehello";
		char[] entry = string.toCharArray();
		zipList.push(entry);
		String string2 = "women";
		entry = string2.toCharArray();
		zipList.insert(entry, 0, true);
		assertEquals(string, new String(zipList.get(0)));
		assertEquals(string2, new String(zipList.get(1)));
	}
	
	@Test
	public void testIterator(){
		ZipList zipList = new ZipList();
		String string = "sgedhtgrherjrtyjtyukerthwerutykyujtgehrwujuytrjtyjherethrhrehello";
		String string2 = "women";
		String string3 = "hello word";
		String[] strings = {string,string2,string3};
		for(String string4 : strings){
			zipList.push(string4.toCharArray());
		}
		Iterator<char[]> iterator = zipList.iterator();
		int i = 0;
		while(iterator.hasNext()){
			assertEquals(strings[i++], new String(iterator.next()));
		}
	}
	
	@Test
	public void testReverseIterator(){
		ZipList zipList = new ZipList();
		String string = "sgedhtgrherjrtyjtyukerthwerutykyujtgehrwujuytrjtyjherethrhrehello";
		String string2 = "women";
		String string3 = "hello word";
		String[] strings = {string,string2,string3};
		String[] strings2 = {string3,string2,string};
		for(String string4 : strings){
			zipList.push(string4.toCharArray());
		}
		Iterator<char[]> iterator = zipList.reverseIterator();
		
		int i = 0;
		while(iterator.hasNext()){
			assertEquals(strings2[i++], new String(iterator.next()));
		}
	}
	
	@Test
	public void testMerge(){
		ZipList list1 = new ZipList(), list2 = new ZipList(),list3 = new ZipList();
		String[] strings = {"abc","def","higklmn","weth","wo"};
		int i = 0;
		for(;i < 3;++i){
			list1.push(strings[i].toCharArray());
			list3.push(strings[i].toCharArray());
		}
		for(;i < 5; ++i){
			list2.push(strings[i].toCharArray());
			list3.push(strings[i].toCharArray());
		}
		ZipList list = list1.merge(list2);
		assertEquals(list3, list);
	}
	
	
	@Test
	public void testEquals(){
		ZipList list1 = new ZipList(), list2 = new ZipList();
		String[] strings = {"abc","def","higklmn","weth","wo"};
		assertEquals(list1, list2);
		for(String string : strings){
			list1.push(string.toCharArray());
			list2.push(string.toCharArray());
		}
		assertEquals(list1, list2);
		list1.remove(0);
		assertNotEquals(list1, list2);
	}
	
	@Test
	public void testFind(){
		ZipList list1 = new ZipList();
		String[] strings = {"abc","def","higklmn","weth","wo"};
		for(String string : strings){
			list1.push(string.toCharArray());
		}
		assertEquals(list1.find("def".toCharArray()), 1);
		assertEquals(list1.find("zzzz".toCharArray()), -1);
	}
}
