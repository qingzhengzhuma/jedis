package jedis.client;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import jedis.util.CommandLine;

public class JedisWorkbench {
	private SocketChannel clientSocket;
	private String serverIP;
	private int serverPort;
	
	private void parseConfigLine(String line){
		if(line != null && line.length() != 0){
			String[] segs = line.split(":");
			if(segs.length == 2){
				if(segs[0].equals("serverIP")){
					this.serverIP = segs[1];
				}else if(segs[0].equals("serverPort")){
					this.serverPort = Integer.parseInt(segs[1]);
				}
			}
		}
		
	}
	
	private void loadConfig(String confPathName) throws IOException{
		RandomAccessFile configFile = new RandomAccessFile(confPathName, "r");
		String line = null;
		while((line=configFile.readLine()) != null){
			parseConfigLine(line);
		}
		configFile.close();
	}

	private void init() throws IOException {
		loadConfig("client.conf");
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println();
				if(clientSocket != null && clientSocket.isOpen()){
					try{
						clientSocket.close();
					}catch(IOException e){
						e.printStackTrace();
					}
					
				}
			}
		});
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

	public void connect() {
		try {
			init();
		} catch (IOException e) {
			System.out.println("Failed to read configuration");
			System.exit(-1);
		}
		try {
			clientSocket = SocketChannel.open();
			clientSocket.configureBlocking(true);
			clientSocket.connect(new InetSocketAddress(this.serverIP, this.serverPort));
			clientSocket.finishConnect();
		} catch (IOException e) {
			System.out.println("Failed to connect to server");
			System.exit(-1);
		}
	}

	public void start() {
		String promptSymbol = this.serverIP + ":" + this.serverPort + ">";
		Scanner scanner = new Scanner(System.in);
		System.out.print(promptSymbol);
		try {
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				CommandLine cl = new CommandLine();
				if (cl.parse(line)) {
					String command = cl.getNormalizedCmd();
					if (command.equals("quit") || command.equals("exit")) {
						clientSocket.close();
						System.out.println("Bye");
						break;
					}else {
						command = cl.getNormalizedCmdLine();
						ByteBuffer buffer = wrapCommandToBuffer(command);
						while (buffer.hasRemaining()) {
							clientSocket.write(buffer);
						}
						ByteBuffer readBuffer = ByteBuffer.allocate(1024);
						if (clientSocket.read(readBuffer) == -1) {
							System.out.println("Connection closed!");
							System.exit(-1);
						}
						readBuffer.flip();
						System.out.println(new String(readBuffer.array()));
					}
				}
				System.out.print(promptSymbol);
			}
			scanner.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Bye");
		}
	}

	public static void main(String[] args) {
		JedisWorkbench client = new JedisWorkbench();
		client.connect();
		client.start();
	}
}
