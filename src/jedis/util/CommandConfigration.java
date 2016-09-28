package jedis.util;

import jedis.util.CommandRule;

public class CommandConfigration {
	private static final CommandRule[] commandRules = {
			new CommandRule("ping", 0, 0,new PingHandler()),
			new CommandRule("select", 1, 1,new SelectHandler()),
			new CommandRule("set", 2, 2,new SetHandler()),
			new CommandRule("get", 1, 1,new GetHandler())
		};
	
	public static CommandRule[] getCommandRules(){
		return commandRules;
	}
	
	public static boolean verifyCommand(String command,int argc){
		for(CommandRule rule : CommandConfigration.getCommandRules()){
			if(command.equals(rule.getCommand()) && 
					argc >= rule.getMinArgc() && 
					argc <= rule.getMaxArgc()) return true;
		}
		return false;
	}
}
