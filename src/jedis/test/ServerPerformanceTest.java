package jedis.test;

import java.io.IOException;

import org.junit.Test;

import jedis.client.JedisWorkbench;

public class ServerPerformanceTest {

	@Test
	public void test(){
		try {
			JedisWorkbench workbench = new JedisWorkbench();
			workbench.connect();
			for(int i = 0; i < 60000;++i){
				String cmd = "set " + Integer.toString(i) + " " + Integer.toString(i + 1);
				workbench.sendRequest(cmd);
			}
			workbench.close();
		} catch (IOException e) {
			// TODO: handle exception
		}
	}
}
