package jedis.util;

public class ExistsHandler implements CommandHandler {

	@Override
	public JedisObject execute(JedisDB[] databases, JedisClient client, CommandLine cl)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		Sds key = new Sds(cl.getArg(0));
		if(databases[client.getCurrntDB()].containsKey(key)){
			return MessageConstant.YES;
		}else{
			return MessageConstant.NO;
		}
	}

}
