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

public class Client {
	
	private Selector connectionSelector;
	private Selector dataSelector;
	private SocketChannel clientSocket;
	
	public boolean init(){
		try {
			connectionSelector = Selector.open();
			dataSelector = Selector.open();
			clientSocket = SocketChannel.open();
			clientSocket.configureBlocking(false);
			clientSocket.register(connectionSelector, SelectionKey.OP_CONNECT);
			clientSocket.register(dataSelector, SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return true;
	}
	
	void handleConnectedEvent(SelectionKey key){
		try {
			clientSocket.finishConnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start(){
		try {
			clientSocket.connect(new InetSocketAddress("127.0.0.1", 8081));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void main(String[] args){
		Client client = new Client();
		try {
			client.clientSocket = SocketChannel.open();
			client.clientSocket.configureBlocking(true);
			client.dataSelector = Selector.open();
			client.clientSocket.connect(new InetSocketAddress("127.0.0.1", 8081));
			if(client.clientSocket.finishConnect()){
				System.out.println("Connected...");
			}else{
				System.out.println("Failed to connect");
			}
			
			client.clientSocket.configureBlocking(false);
			client.clientSocket.register(client.dataSelector, SelectionKey.OP_READ);
			Scanner scanner = new Scanner(System.in);
			while(scanner.hasNext()){
				String line = scanner.nextLine();
				byte[] command = line.trim().getBytes();
				System.out.println("Got : " + line);
				int length = command.length;
				ByteBuffer buffer = ByteBuffer.allocate(length + 4);
				byte[] lenghtByte = new byte[4];
				lenghtByte[0]= (byte)(length | (255<<24));
				lenghtByte[1]= (byte)(length | (255<<16));
				lenghtByte[2]= (byte)(length | (255<<8));
				lenghtByte[3]= (byte)(length | 255);
				buffer.put(lenghtByte);
				buffer.put(command);
				buffer.flip();
				while(buffer.hasRemaining()){
					client.clientSocket.write(buffer);
				}
				client.dataSelector.select();
				Set<SelectionKey> selectionKeys = client.dataSelector.selectedKeys();
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
			}
			scanner.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
