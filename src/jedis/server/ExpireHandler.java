package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisObject;
import jedis.util.Sds;

public class ExpireHandler implements CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		int curDB = client.getCurrntDB();
		Sds key = new Sds(cl.getArg(0));
		if(Server.inUseDatabases[curDB].containsKey(key)){
			long now = System.currentTimeMillis();
			try{
				long t = (long)Double.parseDouble(cl.getArg(1)) * 1000;
				Server.expireKeys[curDB].put(key, now + t);
				return MessageConstant.OK;
			}catch (NumberFormatException e) {
				// TODO: handle exception
				throw new UnsupportedOperationException();
			}
		}
		return MessageConstant.NO_SUCH_KEY;
	}

}
