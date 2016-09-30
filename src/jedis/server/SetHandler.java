package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisObject;
import jedis.util.MessageConstant;
import jedis.util.Sds;

public class SetHandler implements CommandHandler{

	@Override
	public JedisObject execute(JedisClient client,
			CommandLine cl) throws UnsupportedOperationException{
		// TODO Auto-generated method stub
		int curDB = client.getCurrntDB();
		JedisDB db = Server.inUseDatabases[curDB];
		Sds key = new Sds(cl.getArg(0));
		JedisObject value = new Sds(cl.getArg(1));
		if(db.containsKey(key)){
			JedisObject object = db.get(key);
			if(!(object instanceof Sds)) throw new UnsupportedOperationException();
		}
		db.set(key, value);
		Server.aof.put(cl,curDB);
		return MessageConstant.OK;
	}
}
