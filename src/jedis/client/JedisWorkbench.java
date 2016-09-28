package jedis.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import jedis.util.CommandConfigration;
import jedis.util.CommandLine;
import jedis.util.CommandRule;

public class JedisWorkbench {
	private SocketChannel clientSocket;
	private String serverIP;
	private int serverPort;
	
	
	
	private void init(){
		this.serverIP = "127.0.0.1";
		this.serverPort = 8081;
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run() {
				System.out.println();
			}
		});
	}
	
	private boolean writeCommandLengthToBuffer(ByteBuffer buffer,int length){
		buffer.put((byte)(length | (255<<24)));
		buffer.put((byte)(length | (255<<16)));
		buffer.put((byte)(length | (255<<8)));
		buffer.put((byte)(length | 255));
		return true;
	}
	
	private boolean writeCommandToBuffer(ByteBuffer buffer,String command){
		int length = command.length();
		for(int i = 0; i < length;++i){
			buffer.put((byte)command.charAt(i));
		}
		return true;
	}
	
	private ByteBuffer wrapCommandToBuffer(String command){
		if(command == null || command.length() == 0) return ByteBuffer.allocate(0);
	    int length = command.length();
	    ByteBuffer buffer = ByteBuffer.allocate(length + 4);
	    //writeCommandLengthToBuffer(buffer, length);
	    writeCommandToBuffer(buffer, command);
	    buffer.flip();
		return buffer;
	}
	
	public boolean connect(){
		init();
		try {
			clientSocket = SocketChannel.open();
			clientSocket.configureBlocking(true);
			clientSocket.connect(new InetSocketAddress(this.serverIP,this.serverPort));
			clientSocket.finishConnect();
		} catch (IOException e) {
			System.out.println("Failed to connect to server");
			System.exit(-1);
		}
		return true;
	}
	
	public void start(){
		String promptSymbol = this.serverIP + ":" + this.serverPort + ">";
		Scanner scanner = new Scanner(System.in);
		System.out.print(promptSymbol);
		try {
			while(scanner.hasNext()){
				String line = scanner.nextLine();
				if(CommandLine.parse(line)){
					String command = CommandLine.getCommand().toLowerCase();
					int argc = CommandLine.getArgc();
					if(CommandConfigration.verifyCommand(command,argc) == true){
						command = CommandLine.getNormalizedCmdLine();
						ByteBuffer buffer = wrapCommandToBuffer(command);
						while(buffer.hasRemaining()){
							clientSocket.write(buffer);
						}
						ByteBuffer readBuffer = ByteBuffer.allocate(1024);
						if(clientSocket.read(readBuffer) == -1){
							System.out.println("Disconnected...");
							System.exit(-1);
						}
						readBuffer.flip();
						System.out.println(new String(readBuffer.array()));
					}else if(command.equals("exit")){
						clientSocket.close();
					}else{
						System.out.println("ILLIGAL COMMAND");
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
	
	public static void main(String[] args){
		JedisWorkbench client = new JedisWorkbench();
		client.connect();
		client.start();
	}
}
