package jedis.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class JedisWorkbench {
	private Selector clientSelector;
	private SocketChannel clientSocket;
	private String serverIP;
	private int serverPort;
	
	private void init(){
		this.serverIP = "127.0.0.1";
		this.serverPort = 8081;
	}
	
	private String prepareCommand(String command){
		String preparedCommand = command == null ? new String() : command.trim();
		if(preparedCommand.length() > 0 && preparedCommand.charAt(0) == '>'){
			preparedCommand = preparedCommand.substring(1);
		}
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
			clientSelector = Selector.open();
			clientSocket.connect(new InetSocketAddress(this.serverIP,this.serverPort));
			clientSocket.finishConnect();
			clientSocket.configureBlocking(false);
			clientSocket.register(clientSelector, SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return true;
	}
	
	public boolean blockConnect(){
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
		try {
			Scanner scanner = new Scanner(System.in);
			System.out.print(">");
			while(scanner.hasNext()){
				String line = scanner.nextLine();
				String command = prepareCommand(line);
				System.out.println(line);
				System.out.println(command);
				ByteBuffer buffer = wrapCommandToBuffer(command);
				while(buffer.hasRemaining()){
					clientSocket.write(buffer);
				}
				clientSelector.select();
				Set<SelectionKey> selectionKeys = clientSelector.selectedKeys();
				Iterator<SelectionKey> iterator = selectionKeys.iterator();
				while(iterator.hasNext()){
					SelectionKey key = iterator.next();
					if(key.isReadable()){
						SocketChannel channel = (SocketChannel) key.channel();
						ByteBuffer readBuffer = ByteBuffer.allocate(1024);
						if(channel.read(readBuffer) == -1){
							System.out.println("Disconnected...");
							System.exit(-1);
						}
						readBuffer.flip();
						System.out.println("receive:" + new String(readBuffer.array()));
					}
					iterator.remove();
				}
				System.out.print(">");
			}
			scanner.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void blockStart(){
		String promptSymbol = this.serverIP + ":" + this.serverPort + ">";
		Scanner scanner = new Scanner(System.in);
		System.out.print(promptSymbol);
		try {
			while(scanner.hasNext()){
				String line = scanner.nextLine();
				String command = prepareCommand(line);
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
		client.blockConnect();
		client.blockStart();
	}
}
