package jedis.util;

public class PingHandler implements CommandHandler{
	public JedisObject execute(JedisDB[]database,JedisClient client,String command){
		return new Sds("PONG");
	}
}
