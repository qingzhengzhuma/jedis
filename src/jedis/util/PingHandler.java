package jedis.util;

public class PingHandler implements CommandHandler{
	public JedisObject execute(JedisDB[]database,JedisClient client,CommandLine cl){
		return MessageConstant.PONG;
	}
}
