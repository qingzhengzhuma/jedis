package jedis.server;

import jedis.util.CommandLine;
import jedis.util.JedisObject;
import jedis.util.Sds;

abstract public class CommandHandler {
	private static final CommandRule[] commandRules = {
			new CommandRule("ping", 0, 0,new PingHandler()),
			new CommandRule("select", 1, 1,new SelectHandler()),
			new CommandRule("set", 2, 2,new SetHandler()),
			new CommandRule("get", 1, 1,new GetHandler()),
			new CommandRule("exists", 1, 1, new ExistsHandler()),
			new CommandRule("append", 2, 2, new AppendHandler()),
			new CommandRule("del", 1, Integer.MAX_VALUE, new DeleteHandle()),
			new CommandRule("save", 0, 0, new SaveHandler()),
			new CommandRule("expire", 2, 2, new ExpireHandler()),
		};
	
	private static CommandHandler unsupportCommandHandler = new CommandHandler(){

		@Override
		public JedisObject execute(JedisClient client, CommandLine cl)
				throws UnsupportedOperationException {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}
	};
	
	public static CommandHandler getHandler(String cmd){
		for(CommandRule rule : commandRules){
			if(rule.getCommand().equals(cmd)){
				return rule.getHandler();
			}
		}
		return unsupportCommandHandler;
	}
	
	public static CommandRule[] getCommandRules(){
		return commandRules;
	}
	public static boolean verifyCommand(String command,int argc){
		for(CommandRule rule : commandRules){
			if(command.equals(rule.getCommand()) && 
					argc >= rule.getMinArgc() && 
					argc <= rule.getMaxArgc()) return true;
		}
		return false;
	}
	
	abstract public JedisObject execute(JedisClient client,CommandLine cl) 
			throws UnsupportedOperationException;
}

class AppendHandler extends CommandHandler{
	
	@Override
	public JedisObject execute(JedisClient client,
			CommandLine cl) throws UnsupportedOperationException{
		int curDB = client.getCurrntDB();
		JedisDB db = Server.inUseDatabases[curDB];
		Sds key = new Sds(cl.getArg(0));
		Server.removeIfExpired(key, curDB);
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
		if(Server.aofState == AofState.AOF_ON){
			Server.aof.put(cl,curDB);
		}
		return MessageConstant.OK;
	}
}

class BgSaveHandler extends CommandHandler{

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl) 
			throws UnsupportedOperationException {
		if(Server.aofSaveThread.isAlive()) return MessageConstant.AOF_BUSY;
		int dbNum = Server.inUseDatabases.length;
		JedisDB[] dbs = new JedisDB[dbNum];
		for(int i = 0; i < dbNum;++i){
			dbs[i] = Server.inUseDatabases[i].copy();
		}
		Server.rdbSaveThread = new RdbSaveThread(dbs);
		Server.rdbSaveThread.start();
		return MessageConstant.OK;
	}

}

class DeleteHandle extends CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl)
			throws UnsupportedOperationException {
		int keyCount = cl.getArgc();
		int deletedCount = 0;
		int curDB = client.getCurrntDB();
		for(int i = 0; i < keyCount;++i){
			Sds key = new Sds(cl.getArg(i));
			Server.removeIfExpired(key, curDB);
			if(Server.inUseDatabases[curDB].remove(key)){
				++deletedCount;
			}
		}
		if(Server.aofState == AofState.AOF_ON){
			Server.aof.put(cl,curDB);
		}
		if(deletedCount < MessageConstant.NUMBER_COUNT){
			return MessageConstant.NUMBERS[deletedCount];
		}
		return new Sds(Integer.toString(deletedCount));
	}

}

class ExistsHandler extends CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		Sds key = new Sds(cl.getArg(0));
		int curDB = client.getCurrntDB();
		Server.removeIfExpired(key, curDB);
		if(Server.inUseDatabases[curDB].containsKey(key)){
			return MessageConstant.YES;
		}else{
			return MessageConstant.NO;
		}
	}

}

class ExpireHandler extends CommandHandler {

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

class GetHandler extends CommandHandler {

	@Override
	public JedisObject execute( JedisClient client,CommandLine cl) {
		// TODO Auto-generated method stub
		int curDB = client.getCurrntDB();
		Sds key = new Sds(cl.getArg(0));
		Server.removeIfExpired(key, curDB);
		return Server.inUseDatabases[curDB].get(key);
	}

}

class PingHandler extends CommandHandler{
	public JedisObject execute(JedisClient client,CommandLine cl){
		return MessageConstant.PONG;
	}
}

class SaveHandler extends CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		RDB.save(Server.inUseDatabases, JedisConfigration.rdbPathName);
		return MessageConstant.OK;
	}

}

class SelectHandler extends CommandHandler {
	
	@Override
	public JedisObject execute(JedisClient client,CommandLine cl) 
			throws UnsupportedOperationException,IllegalArgumentException{
		
		try{
			int dbIndex = Integer.parseInt(cl.getArg(0));
			
			JedisDB[] databases = Server.inUseDatabases;
			if(databases == null || databases.length == 0 || 
					dbIndex < 0 || dbIndex >= databases.length){
				return MessageConstant.INDEX_OUT_OF_BOUNDARY;
			}
			client.setCurrentDB(dbIndex);
			return MessageConstant.OK;
		}catch (NumberFormatException e) {
			// TODO: handle exception
			throw new IllegalArgumentException();
		}
	}

}



class SetHandler extends CommandHandler{

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


class TypeHandler extends CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		int curDB = client.getCurrntDB();
		Sds key = new Sds(cl.getArg(0));
		Server.removeIfExpired(key, curDB);
		JedisObject value = Server.inUseDatabases[curDB].get(key);
		if(value != null){
			return value.type();
		}
		return MessageConstant.NO_SUCH_KEY;
	}

}




