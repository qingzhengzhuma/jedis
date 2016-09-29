package jedis.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import jedis.util.CommandLine;

public class CommandLineTest {
	
	CommandLine commandLine;
	
	@Before
	public void setUp(){
		commandLine = new CommandLine();
	}

	@Test
	public void testParseString() {
		assertEquals(commandLine.parse("   "), false);
		assertEquals(commandLine.parse(""), false);
		assertEquals(commandLine.parse(" ping  "), true);
		assertEquals(commandLine.parse(" ping  	12 6"), true);
	}
	
	@Test
	public void testParseByteArray() {
		assertEquals(commandLine.parse("   ".getBytes()), false);
		assertEquals(commandLine.parse("".getBytes()), false);
		assertEquals(commandLine.parse(" ping  ".getBytes()), true);
		assertEquals(commandLine.parse(" ping  	12 6".getBytes()), true);
	}

	@Test
	public void testGetArgc() {
		commandLine.parse(" ping  	12 6".getBytes());
		assertEquals(2,commandLine.getArgc());
		commandLine.parse(" ping  	".getBytes());
		assertEquals(0,commandLine.getArgc());
	}

	@Test
	public void testGetCommand() {
		commandLine.parse(" ping  	12                     6".getBytes());
		assertEquals("ping",commandLine.getCommand());
		commandLine.parse(" Ping  	12                     6".getBytes());
		assertEquals("Ping",commandLine.getCommand());
	}
	
	@Test
	public void testGetNormalizedCommand() {
		commandLine.parse(" ping  	12                     6".getBytes());
		assertEquals("ping",commandLine.getNormalizedCmd());
		commandLine.parse(" Ping  	12                     6".getBytes());
		assertEquals("ping",commandLine.getNormalizedCmd());
	}

	@Test
	public void testGetNormalizedCmdLine() {
		commandLine.parse(" Ping  	12                     6".getBytes());
		assertEquals("ping 12 6",commandLine.getNormalizedCmdLine());
	}

}
