package jedis.server;

import java.util.LinkedList;
import java.util.List;

import jedis.util.CommandLine;
import jedis.util.JedisObject;
import jedis.util.Sds;

abstract public class CommandHandler {
	public static final CommandHandler multiHandler = new MultiHandler();
	public static final CommandHandler execHandler = new ExecHandler();
	private static final CommandRule[] commandRules = { new CommandRule("ping", 0, 0, new PingHandler()),
			new CommandRule("select", 1, 1, new SelectHandler()), new CommandRule("set", 2, 2, new SetHandler()),
			new CommandRule("get", 1, 1, new GetHandler()), new CommandRule("exists", 1, 1, new ExistsHandler()),
			new CommandRule("append", 2, 2, new AppendHandler()),
			new CommandRule("del", 1, Integer.MAX_VALUE, new DeleteHandle()),
			new CommandRule("save", 0, 0, new SaveHandler()), new CommandRule("bgsave", 0, 0, new BgSaveHandler()),
			new CommandRule("expire", 2, 2, new ExpireHandler()),
			new CommandRule("watch", 1, Integer.MAX_VALUE, new WatchHandler()),
			new CommandRule("unwatch", 0, 0, new UnwatchHandler()), new CommandRule("multi", 0, 0, multiHandler),
			new CommandRule("exec", 0, 0, execHandler) ,
			new CommandRule("subscribe", 1, Integer.MAX_VALUE, new SubscribeHandler()),
			new CommandRule("publish", 2, 2, new PublishHandler()),
			new CommandRule("monitor", 0, 0, new MonitorHandler())};

	private static CommandHandler unsupportCommandHandler = new CommandHandler() {

		@Override
		public JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}
	};

	protected void touchWatchKey(JedisDB db, Sds key) {
		List<JedisClient> clients = db.watchedKeys.get(key);
		if (clients != null) {
			for (JedisClient c : clients) {
				c.dirtyCas = true;
			}
		}
	}

	public static boolean executeCommand(JedisClient client, byte[] data) {
		CommandLine cl = new CommandLine();
		boolean state = false;
		if (cl.parse(new String(data))) {
			String command = cl.getNormalizedCmd();
			int argc = cl.getArgc();
			if (CommandHandler.verifyCommand(command, argc) == true) {
				try {
					CommandHandler handler = CommandHandler.getHandler(client, command);
					JedisObject object = handler.execute(client, cl);
					if (object == null)
						object = MessageConstant.NIL;
					state = true;
					client.pushResult(object);
				} catch (UnsupportedOperationException e) {
					// TODO: handle exception
					client.pushResult(MessageConstant.NOT_SUPPORTED_OPERATION);
				} catch (IllegalArgumentException e) {
					// TODO: handle exception
					client.pushResult(MessageConstant.ILLEGAL_ARGUMENT);
				}
			}
		}else{
			client.pushResult(MessageConstant.ILLEGAL_COMMAND);
		}
		return state;
	}

	public static CommandHandler getHandler(JedisClient client, String cmd) {
		if (client.multiState != MultiState.NONE) {
			if (cmd.equals("exec")) return execHandler;	
			return multiHandler;
		}
		for (CommandRule rule : commandRules) {
			if (rule.getCommand().equals(cmd)) {
				return rule.getHandler();
			}
		}
		return unsupportCommandHandler;
	}

	public static CommandRule[] getCommandRules() {
		return commandRules;
	}

	public static boolean verifyCommand(String command, int argc) {
		for (CommandRule rule : commandRules) {
			if (command.equals(rule.getCommand()) && argc >= rule.getMinArgc() && argc <= rule.getMaxArgc())
				return true;
		}
		return false;
	}

	abstract protected JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException;
}

class AppendHandler extends CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		JedisDB db = client.db;
		Sds key = new Sds(cl.getArg(0));
		db.removeIfExpired(key);
		JedisObject value;
		if (db.containsKey(key)) {
			value = db.get(key);
			if (!(value instanceof Sds))
				throw new UnsupportedOperationException();
			else {
				((Sds) value).append(cl.getArg(1));
			}
		} else {
			value = new Sds(cl.getArg(1));
		}
		db.set(key, value);
		touchWatchKey(db, key);
		if (Server.aofState == AofState.AOF_ON) {
			Server.aof.put(cl, db);
		}
		return MessageConstant.OK;
	}
}

// TODO: removed expired keys before save
class BgSaveHandler extends CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		if (Server.aofSaveThread != null && Server.aofSaveThread.isAlive()) {
			return MessageConstant.AOF_BUSY;
		}

		if (Server.rdbSaveThread != null && Server.rdbSaveThread.isAlive()) {
			return MessageConstant.RDB_BUSY;
		}

		int dbNum = Server.inUseDatabases.length;
		JedisDB[] dbs = new JedisDB[dbNum];
		for (int i = 0; i < dbNum; ++i) {
			dbs[i] = Server.inUseDatabases[i].copy();
		}
		Server.rdbSaveThread = new RdbSaveThread(dbs);
		Server.rdbSaveThread.start();
		return MessageConstant.OK;
	}

}

class DeleteHandle extends CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		int keyCount = cl.getArgc();
		int deletedCount = 0;
		JedisDB db = client.db;
		for (int i = 0; i < keyCount; ++i) {
			Sds key = new Sds(cl.getArg(i));
			db.removeIfExpired(key);
			if (db.remove(key)) {
				++deletedCount;
			}
			touchWatchKey(db, key);
		}
		if (Server.aofState == AofState.AOF_ON) {
			Server.aof.put(cl, db);
		}
		if (deletedCount < MessageConstant.NUMBER_COUNT) {
			return MessageConstant.NUMBERS[deletedCount];
		}
		return new Sds(Integer.toString(deletedCount));
	}

}

class ExecHandler extends CommandHandler {

	@Override
	protected JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		if (client.multiState == MultiState.NONE) {
			return MessageConstant.EXEC_WITHOUT_MULTI;
		}
		if (client.multiState == MultiState.ERROR_HAPPENED) {
			client.multiState = MultiState.NONE;
			client.clearWatch();
			return MessageConstant.ERROR_HAPPED_IN_TRANSACTIOIN;
		}
		client.multiState = MultiState.NONE;
		Sds result = new Sds(MessageConstant.MULTI_COMMAND_EMPTY);
		if (client.multiCommandBuf.size() > 0) {
			int i = 1;
			result = new Sds();
			while (!client.multiCommandBuf.isEmpty()) {
				CommandLine c = client.multiCommandBuf.poll();
				String cmd = c.getNormalizedCmd();
				if(c.getArgc() > 0 && client.dirtyCas == true && 
				   client.watchedKeys.contains(new Sds(c.getArg(0)))){
					result = MessageConstant.NIL;
					break;
				}
				CommandHandler handler = getHandler(client, cmd);
				try {
					result.append(Integer.toString(i));
					result.append(")  ");
					byte[] res = handler.execute(client, c).getBytes();
					result.append(res);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					result.append("ERROR");
				}
				result.append("\r\n");
				++i;
			}
		}
		client.multiCommandBuf.clear();
		
		client.clearWatch();
		return result;
	}
}

class ExistsHandler extends CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		Sds key = new Sds(cl.getArg(0));
		JedisDB db = client.db;
		db.removeIfExpired(key);
		if (db.containsKey(key)) {
			return MessageConstant.YES;
		} else {
			return MessageConstant.NO;
		}
	}

}

class ExpireHandler extends CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		JedisDB db = client.db;
		Sds key = new Sds(cl.getArg(0));
		db.removeIfExpired(key);
		if (db.containsKey(key)) {
			long now = System.currentTimeMillis();
			try {
				long t = (long) Double.parseDouble(cl.getArg(1)) * 1000;
				db.expireKeys.put(key, now + t);
				return MessageConstant.OK;
			} catch (NumberFormatException e) {
				// TODO: handle exception
				throw new UnsupportedOperationException();
			}
		}
		return MessageConstant.NO_SUCH_KEY;
	}

}

class GetHandler extends CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl) {
		// TODO Auto-generated method stub
		JedisDB db = client.db;
		Sds key = new Sds(cl.getArg(0));
		db.removeIfExpired(key);
		JedisObject value = db.get(key);
		if (value == null)
			value = MessageConstant.NIL;
		return value;
	}

}

class MonitorHandler extends CommandHandler{

	@Override
	protected JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		Server.monitors.add(client);
		return MessageConstant.OK;
	}
	
}

class PingHandler extends CommandHandler {
	public JedisObject execute(JedisClient client, CommandLine cl) {
		return MessageConstant.PONG;
	}
}

class PublishHandler extends CommandHandler{

	@Override
	protected JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		String channel = cl.getArg(0);
		String message = cl.getArg(1);
		List<JedisClient> clients = Server.subscribedChannels.get(new Sds(channel));
		int count = 0;
		if(clients != null){
			Sds response = new Sds("1) \"message\"\n");
			response.append("2) \"");
			response.append(channel);
			response.append("\"\n3) ");
			response.append(message);
			response.append("\n");
			for(JedisClient c : clients){
				c.pushResult(response);
				c.sendResponse();
				++count;
			}
		}
		return new Sds(Integer.toString(count));
	}	
}

class MultiHandler extends CommandHandler {

	@Override
	protected JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		if (client.multiState == MultiState.NONE) {
			client.multiState = MultiState.WAIT_EXEC;
			return MessageConstant.OK;
		} else if (cl.getNormalizedCmd().equals("multi")) {
			return MessageConstant.MULTI_CANNOT_BE_NEST;
		}
		client.multiCommandBuf.offer(cl);
		return MessageConstant.QUEUED;
	}
}

class SaveHandler extends CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		RDB.save(Server.inUseDatabases, JedisConfigration.rdbPathName);
		return MessageConstant.OK;
	}

}

class SelectHandler extends CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl)
			throws UnsupportedOperationException, IllegalArgumentException {

		try {
			int dbIndex = Integer.parseInt(cl.getArg(0));

			JedisDB[] databases = Server.inUseDatabases;
			if (databases == null || databases.length == 0 || dbIndex < 0 || dbIndex >= databases.length) {
				return MessageConstant.INDEX_OUT_OF_BOUNDARY;
			}
			client.db = databases[dbIndex];
			return MessageConstant.OK;
		} catch (NumberFormatException e) {
			// TODO: handle exception
			throw new IllegalArgumentException();
		}
	}

}

class SetHandler extends CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		JedisDB db = client.db;
		Sds key = new Sds(cl.getArg(0));
		db.removeIfExpired(key);
		JedisObject value = new Sds(cl.getArg(1));
		db.set(key, value);
		if (Server.aofState == AofState.AOF_ON) {
			Server.aof.put(cl, db);
		}
		touchWatchKey(db, key);
		return MessageConstant.OK;
	}
}

class SubscribeHandler extends CommandHandler{

	@Override
	protected JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		int channelCount = cl.getArgc();
		for(int i = 0; i < channelCount;++i){
			Sds channel = new Sds(cl.getArg(i));
			List<JedisClient> clients = Server.subscribedChannels.get(channel);
			if(clients == null){
				clients = new LinkedList<>();
				Server.subscribedChannels.put(channel, clients);
			}
			client.subscriedChannel.add(channel);
			clients.add(client);
		}
		return MessageConstant.OK;
	}
}

class TypeHandler extends CommandHandler {

	@Override
	public JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		JedisDB db = client.db;
		Sds key = new Sds(cl.getArg(0));
		db.removeIfExpired(key);
		JedisObject value = db.get(key);
		if (value != null) {
			return value.type();
		}
		return MessageConstant.NO_SUCH_KEY;
	}
}

class WatchHandler extends CommandHandler {

	@Override
	protected JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		if (client.multiState != MultiState.NONE) {
			return MessageConstant.WATCH_INSIDE_MULTI_NOT_ALLOWED;
		}
		client.watch(cl);
		return MessageConstant.OK;
	}
}

class UnwatchHandler extends CommandHandler {

	@Override
	protected JedisObject execute(JedisClient client, CommandLine cl) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		client.clearWatch();
		return MessageConstant.OK;
	}
}
