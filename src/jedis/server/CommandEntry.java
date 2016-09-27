package jedis.server;

import jedis.util.CommandHandler;

public class CommandEntry{
	String command;
	CommandHandler handler;
	public CommandEntry(String command,CommandHandler handler) {
		// TODO Auto-generated constructor stub
		this.command = command;
		this.handler = handler;
	}
}
