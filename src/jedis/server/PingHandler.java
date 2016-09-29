package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisClient;
import jedis.util.JedisObject;
import jedis.util.MessageConstant;

public class PingHandler implements CommandHandler{
	public JedisObject execute(JedisClient client,CommandLine cl){
		return MessageConstant.PONG;
	}
}
