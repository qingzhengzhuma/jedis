package jedis.util;

public interface CommandHandler {
	public abstract JedisObject execute(JedisDB[]databases,
			JedisClient client,CommandLine cl) 
			throws UnsupportedOperationException;
}
