package jedis.test;

import static org.junit.Assert.*;

import org.junit.Test;

import jedis.util.CommandLine;

public class CommandLineTest {

	@Test
	public void testParseString() {
		assertEquals(CommandLine.parse("   "), false);
		assertEquals(CommandLine.parse(""), false);
		assertEquals(CommandLine.parse(" ping  "), true);
		assertEquals(CommandLine.parse(" ping  	12 6"), true);
	}
	
	@Test
	public void testParseByteArray() {
		assertEquals(CommandLine.parse("   ".getBytes()), false);
		assertEquals(CommandLine.parse("".getBytes()), false);
		assertEquals(CommandLine.parse(" ping  ".getBytes()), true);
		assertEquals(CommandLine.parse(" ping  	12 6".getBytes()), true);
	}

	@Test
	public void testGetArgc() {
		CommandLine.parse(" ping  	12 6".getBytes());
		assertEquals(2,CommandLine.getArgc());
		CommandLine.parse(" ping  	".getBytes());
		assertEquals(0,CommandLine.getArgc());
	}

	@Test
	public void testGetCommand() {
		CommandLine.parse(" ping  	12                     6".getBytes());
		assertEquals("ping",CommandLine.getCommand());
		CommandLine.parse(" Ping  	12                     6".getBytes());
		assertEquals("Ping",CommandLine.getCommand());
	}
	
	@Test
	public void testGetNormalizedCommand() {
		CommandLine.parse(" ping  	12                     6".getBytes());
		assertEquals("ping",CommandLine.getNormalizedCmd());
		CommandLine.parse(" Ping  	12                     6".getBytes());
		assertEquals("ping",CommandLine.getNormalizedCmd());
	}

	@Test
	public void testGetNormalizedCmdLine() {
		CommandLine.parse(" Ping  	12                     6".getBytes());
		assertEquals("ping 12 6",CommandLine.getNormalizedCmdLine());
	}

}
