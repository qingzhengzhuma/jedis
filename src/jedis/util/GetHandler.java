package jedis.util;

public class GetHandler implements CommandHandler {

	@Override
	public JedisObject execute(JedisDB[] databases, JedisClient client,CommandLine cl) {
		// TODO Auto-generated method stub
		JedisDB db = databases[client.getCurrntDB()];
		Sds key = new Sds(cl.getArg(0));
		if(db.containsKey(key)){
			JedisObject object = db.get(key);
			if(!(object instanceof Sds)) throw new UnsupportedOperationException();
			else return (Sds)object;
		}
		return null;
	}

}
