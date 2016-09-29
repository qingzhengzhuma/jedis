package jedis.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import jedis.util.CommandLine;
import jedis.util.JedisConfigration;

public class FakeClient {
	private SocketChannel clientSocket;
	private String serverIP;
	private int serverPort;
	
	public FakeClient(){
		this.serverIP = "127.0.0.1";
		this.serverPort = 8081;
	}

	private boolean writeCommandToBuffer(ByteBuffer buffer, String command) {
		int length = command.length();
		for (int i = 0; i < length; ++i) {
			buffer.put((byte) command.charAt(i));
		}
		return true;
	}

	private ByteBuffer wrapCommandToBuffer(String command) {
		if (command == null || command.length() == 0)
			return ByteBuffer.allocate(0);
		int length = command.length();
		ByteBuffer buffer = ByteBuffer.allocate(length + 4);
		// writeCommandLengthToBuffer(buffer, length);
		writeCommandToBuffer(buffer, command);
		buffer.flip();
		return buffer;
	}
	
	
	private boolean sendCommand(String line) throws IOException {
		CommandLine cl = new CommandLine();
		if (cl.parse(line)) {
			String command = cl.getNormalizedCmd();
			int argc = cl.getArgc();
			if (JedisConfigration.verifyCommand(command, argc) == true) {
				command = cl.getNormalizedCmdLine();
				ByteBuffer buffer = wrapCommandToBuffer(command);
				while (buffer.hasRemaining()) {
					clientSocket.write(buffer);
				}
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				if (clientSocket.read(readBuffer) == -1) {
					throw new IOException();
				}
				readBuffer.flip();
			}
			return true;
		}
		return false;
	}

	public boolean connect() {
		try {
			clientSocket = SocketChannel.open();
			clientSocket.configureBlocking(true);
			clientSocket.connect(new InetSocketAddress(this.serverIP, this.serverPort));
			clientSocket.finishConnect();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean recoverFromAof(String aofPathname) throws IOException {
		AOF aof = AOF.openForRead(aofPathname);
		String line;
		while ((line = aof.next()) != null) {
			sendCommand(line);
		}
		return true;
	}

	public boolean recoverFromRdb(String rdbPathName) {
		if (connect()) {
			return true;
		}
		return false;
	}
}
