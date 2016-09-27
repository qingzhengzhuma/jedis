package jedis.util;

public interface CommandHandler {
	public abstract JedisObject execute(JedisDB[]database,
			JedisClient client,String command) 
			throws UnsupportedOperationException;
}
