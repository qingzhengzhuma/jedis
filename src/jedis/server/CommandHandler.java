package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisObject;

public interface CommandHandler {
	public JedisObject execute(JedisClient client,CommandLine cl) 
			throws UnsupportedOperationException;
}
