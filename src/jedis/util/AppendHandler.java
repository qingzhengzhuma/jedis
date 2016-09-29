package jedis.util;

public class AppendHandler implements CommandHandler{
	
	@Override
	public JedisObject execute(JedisDB[] databases, JedisClient client,
			CommandLine cl) throws UnsupportedOperationException{
		// TODO Auto-generated method stub
		JedisDB db = databases[client.getCurrntDB()];
		Sds key = new Sds(cl.getArg(0));
		JedisObject value;
		if(db.containsKey(key)){
			value = db.get(key);
			if(!(value instanceof Sds)) throw new UnsupportedOperationException();
			else{
				((Sds)value).append(cl.getArg(1));
			}
		}else {
			value = new Sds(cl.getArg(1));
		}
		db.set(key, value);
		return MessageConstant.OK;
	}
}
