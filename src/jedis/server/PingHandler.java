package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisObject;

public class PingHandler implements CommandHandler{
	public JedisObject execute(JedisClient client,CommandLine cl){
		return MessageConstant.PONG;
	}
}
