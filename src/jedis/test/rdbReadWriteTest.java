package jedis.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import jedis.util.JedisConfigration;
import jedis.util.JedisEntry;
import jedis.util.JedisMap;
import jedis.server.JedisDB;
import jedis.util.JedisObject;
import jedis.server.RDB;
import jedis.server.RdbSaveThread;
import jedis.server.Server;
import jedis.util.Sds;

public class rdbReadWriteTest {
	private static JedisDB[] databases;
	private static int TEST_COUNT = 1000000;
	
	@BeforeClass
	public static void setUp(){
		databases = new JedisDB[16];
		for(int i = 0; i < 16;++i){
			databases[i] = new JedisDB();
		}
		for(int i = 1; i <= TEST_COUNT;++i){
			databases[0].set(new Sds(Integer.toString(i)), new Sds(Integer.toString(i+1)));
		}
	}
	
	@Test
	public void test(){
		databases = new JedisDB[16];
		for(int i = 0; i < 16;++i){
			databases[i] = new JedisDB();
		}
		for(int i = 1; i <= TEST_COUNT;++i){
			databases[0].set(new Sds(Integer.toString(i)), new Sds(Integer.toString(i+1)));
		}
	}
	
	@Test
	public void testWriteAndRead(){
		try {
			/*RdbSaveThread thread = new RdbSaveThread(databases);
			thread.start();
			thread.join();*/
			RDB.load(JedisConfigration.rdbPathName);
			JedisDB[] dbs = Server.inUseDatabases;
			assertEquals(databases.length, dbs.length);
			/*Class<?> dbClass = JedisDB.class;
			try {
				Method getDict = dbClass.getDeclaredMethod("getDict");
				getDict.setAccessible(true);
				int i = 0;
				for(JedisDB db : dbs){
					@SuppressWarnings("unchecked")
					Map<Sds, JedisObject> dict1 = (Map<Sds, JedisObject>)getDict.invoke(databases[i++]);
					@SuppressWarnings("unchecked")
					Map<Sds, JedisObject> dict2 = (Map<Sds, JedisObject>)getDict.invoke(db);
					int keyCount1 = dict1.size();
					int keyCount2 = dict2.size();
					System.out.println(keyCount1);
					assertEquals(keyCount1,keyCount2);
					Set<Entry<Sds, JedisObject>> entries = dict1.entrySet();
					for(Entry<Sds, JedisObject> entry : entries){
						assertEquals(dict2.containsKey(entry.getKey()),true);
						assertEquals(dict2.get(entry.getKey()),entry.getValue());
					}
				}
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
		} /*catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
