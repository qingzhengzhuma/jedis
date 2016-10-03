package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisObject;
import jedis.util.Sds;

public class SetHandler implements CommandHandler{

	@Override
	public JedisObject execute(JedisClient client,
			CommandLine cl) throws UnsupportedOperationException{
		// TODO Auto-generated method stub
		int curDB = client.getCurrntDB();
		JedisDB db = Server.inUseDatabases[curDB];
		Sds key = new Sds(cl.getArg(0));
		Server.removeIfExpired(key, curDB);
		JedisObject value = new Sds(cl.getArg(1));
		if(db.containsKey(key)){
			JedisObject object = db.get(key);
			if(!(object instanceof Sds)) throw new UnsupportedOperationException();
		}
		db.set(key, value);
		if(Server.aofState == AofState.AOF_ON){
			Server.aof.put(cl,curDB);
		}
		return MessageConstant.OK;
	}
}
