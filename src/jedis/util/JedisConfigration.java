package jedis.util;

import jedis.util.CommandRule;

public class JedisConfigration {
	private static final CommandRule[] commandRules = {
			new CommandRule("ping", 0, 0,new PingHandler()),
			new CommandRule("select", 1, 1,new SelectHandler()),
			new CommandRule("set", 2, 2,new SetHandler()),
			new CommandRule("get", 1, 1,new GetHandler()),
			new CommandRule("exists", 1, 1, new ExistsHandler()),
			new CommandRule("append", 2, 2, new AppendHandler()),
			new CommandRule("del", 1, Integer.MAX_VALUE, new DeleteHandle())
		};
	
	public static CommandRule[] getCommandRules(){
		return commandRules;
	}
	
	public static boolean verifyCommand(String command,int argc){
		for(CommandRule rule : JedisConfigration.getCommandRules()){
			if(command.equals(rule.getCommand()) && 
					argc >= rule.getMinArgc() && 
					argc <= rule.getMaxArgc()) return true;
		}
		return false;
	}
	
	public static JedisObjectReaderWriter[] getObjectReaders(){
		JedisObjectReaderWriter[]  readers = {
				new SdsReaderWriter(),
		};
		return readers;
	}
	
	public static final String workPath = "/home/liaojian/workspace/";
	public static final String rdbFileName = "jedis.rdb";
	public static final String aofFileName = "jedis.aof";
}
