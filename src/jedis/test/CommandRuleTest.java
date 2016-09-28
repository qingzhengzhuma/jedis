package jedis.test;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import jedis.util.CommandConfigration;
import jedis.util.CommandHandler;
import jedis.util.CommandRule;
import jedis.util.SelectHandler;

public class CommandRuleTest {
	
	private static CommandRule[] commandRules;
	
	@BeforeClass
	public static void setUp(){
		commandRules = CommandConfigration.getCommandRules();
	}
	@Test
	public void testCommandRule() {
		assertNotEquals(null,commandRules);
		assertNotEquals(0, commandRules.length);
	}

	@Test
	public void testGetCommand() {
		assertEquals("ping", commandRules[0].getCommand());
		assertEquals("select", commandRules[1].getCommand());
	}

	@Test
	public void testGetMinArgc() {
		assertEquals(0, commandRules[0].getMinArgc());
	}

	@Test
	public void testGetMaxArgc() {
		assertEquals(0, commandRules[0].getMaxArgc());
	}

	@Test
	public void testGetHandler() {
		int i = 0;
		for(CommandRule rule : commandRules){
			assertEquals(CommandConfigration.getCommandRules()[i++].getHandler(), rule.getHandler());
		}
	}
	
	@Test
	public void testInvariable(){
		CommandRule rule = commandRules[0];
		CommandHandler handler = rule.getHandler();
		handler = new SelectHandler();
	}

}
