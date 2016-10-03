package jedis.server;

import jedis.server.CommandHandler;

public class CommandRule{
	private final String command;
	private final int minArgc;
	private final int maxArgc;
	private CommandHandler handler;
	public CommandRule(String command,int minArgc,int maxArgc,CommandHandler handler) {
		// TODO Auto-generated constructor stub
		this.command = command;
		this.minArgc = minArgc;
		this.maxArgc = maxArgc;
		this.handler = handler;
	}
	
	public String getCommand() {
		return this.command;
	}
	
	public int getMinArgc(){
		return this.minArgc;
	}
	
	public int getMaxArgc(){
		return this.maxArgc;
	}
	
	public CommandHandler getHandler() {
		return this.handler;
	}
}
