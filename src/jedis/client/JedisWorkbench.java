package jedis.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class JedisWorkbench {
	private SocketChannel clientSocket;
	private String serverIP;
	private int serverPort;
	
	private void init(){
		this.serverIP = "127.0.0.1";
		this.serverPort = 8081;
	}
	
	/**
	 * preprecess the command line user input,such trim the space
	 * @param originalCommand
	 * @return 
	 */
	private String prepareCommand(String originalCommand){
		String preparedCommand = originalCommand == null ? new String() : originalCommand.trim();
		return preparedCommand;
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
	    writeCommandLengthToBuffer(buffer, length);
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
			e.printStackTrace();
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
				String command = prepareCommand(line);
				if(command.length() > 0){
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
				}
				System.out.print(promptSymbol);
			}
			scanner.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		JedisWorkbench client = new JedisWorkbench();
		client.connect();
		client.start();
	}
}
