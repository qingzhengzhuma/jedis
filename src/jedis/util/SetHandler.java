package jedis.util;

public class SetHandler implements CommandHandler{

	@Override
	public JedisObject execute(JedisDB[] databases, JedisClient client,
			CommandLine cl) throws UnsupportedOperationException{
		// TODO Auto-generated method stub
		JedisDB db = databases[client.getCurrntDB()];
		Sds key = new Sds(cl.getArg(0));
		JedisObject value = new Sds(cl.getArg(1));
		if(db.containsKey(key)){
			JedisObject object = db.get(key);
			if(!(object instanceof Sds)) throw new UnsupportedOperationException();
		}
		db.set(key, value);
		return MessageConstant.OK;
	}
}
