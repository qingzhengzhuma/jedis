package jedis.test;

import static org.junit.Assert.*;

import org.junit.Test;

import jedis.server.JedisDB;
import jedis.util.Sds;

public class JedisDBTest {

	@Test
	public void testPerformance() {
		JedisDB[] databases = new JedisDB[16];
		for(int i = 0; i < 16;++i){
			databases[i] = new JedisDB();
		}
		for(int i = 1; i <= 100000;++i){
			databases[0].set(new Sds(Integer.toString(i)), new Sds(Integer.toString(i+1)));
		}
	}

}
