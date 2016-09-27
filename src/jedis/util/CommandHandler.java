package jedis.util;

public interface CommandHandler {
	public JedisObject execute(JedisDB[]database,JedisClient client,String command);
}
