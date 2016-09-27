package jedis.util;

public interface CommandHandler {
	public abstract JedisObject execute(JedisDB[]databases,
			JedisClient client,String command) 
			throws UnsupportedOperationException;
}
