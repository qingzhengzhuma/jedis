package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisClient;
import jedis.util.JedisObject;
import jedis.util.MessageConstant;
import jedis.util.Sds;

public class AppendHandler implements CommandHandler{
	
	@Override
	public JedisObject execute(JedisClient client,
			CommandLine cl) throws UnsupportedOperationException{
		// TODO Auto-generated method stub
		JedisDB db = Server.databases[client.getCurrntDB()];
		Sds key = new Sds(cl.getArg(0));
		
		if(Server.aofSaveThread.isAlive()){
			
		}
		
		//once bufDatabases != null, we know data in bufDatabases
		//is newer than in databases, so we firstly check if bufDatabases
		//contain the key and than check the databases, if bufDatabases
		//contain the key, append the value to bufDatabases, else if databases
		//contain the key, generate a new value by append the value to the old
		//value and insert the new value to the bufDatabases, if both datavases
		//contain no key, insert value to the bufDatabases
		if(Server.bufDatabases != null){
			
		}else{
			
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
		}
		return MessageConstant.OK;
	}
}
