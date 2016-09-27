package jedis.test;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.BeforeClass;
import org.junit.Test;

import jedis.server.Server;

public class ServerTest {
	static Server server;
	static Class<?> serverClass;
	
	@BeforeClass
	public static void setUp(){
		server = new Server();
		server.init();
		serverClass = server.getClass();
	}

	@Test
	public void testIsValidCommand(){

		try {
			byte[][] datas = {
					"ping".getBytes(),
					"select".getBytes(),
					"set".getBytes(),
					"get".getBytes(),
					"piNg".getBytes(),
					"  selEct  ".getBytes(),
					"  set".getBytes(),
					"get  ".getBytes()
			};
			Method isValidCommand = serverClass.getDeclaredMethod("isValidCommand", String.class);
			isValidCommand.setAccessible(true);
			Method parseCommand = serverClass.getDeclaredMethod("parseCommand", byte[].class);
			parseCommand.setAccessible(true);
			for(byte[] data : datas){
				String command = (String) parseCommand.invoke(server, data);
				boolean result = (boolean) isValidCommand.invoke(server, command);
				assertEquals(true, result);
			}
			
		} catch (NoSuchMethodException | SecurityException e) {
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
		}
		
	}

}
