package jedis.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import jedis.server.JedisConfigration;
import jedis.server.CommandHandler;
import jedis.server.CommandRule;

public class CommandRuleTest {
	
	private static CommandRule[] commandRules;
	
	@BeforeClass
	public static void setUp(){
		commandRules = CommandHandler.getCommandRules();
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
			assertEquals(CommandHandler.getCommandRules()[i++].getHandler(), rule.getHandler());
		}
	}
}
