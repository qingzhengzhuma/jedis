package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisObject;
import jedis.util.MessageConstant;
import jedis.util.Sds;

public class AppendHandler implements CommandHandler{
	
	@Override
	public JedisObject execute(JedisClient client,
			CommandLine cl) throws UnsupportedOperationException{
		int curDB = client.getCurrntDB();
		JedisDB db = Server.inUseDatabases[curDB];
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
		Server.aof.put(cl,curDB);
		return MessageConstant.OK;
	}
}
