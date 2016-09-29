package jedis.util;

public class TypeHandler implements CommandHandler {

	@Override
	public JedisObject execute(JedisDB[] databases, JedisClient client, CommandLine cl)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		Sds key = new Sds(cl.getArg(0));
		JedisObject value = databases[client.getCurrntDB()].get(key);
		if(value != null){
			return value.type();
		}
		return MessageConstant.NO_SUCH_KEY;
	}

}
