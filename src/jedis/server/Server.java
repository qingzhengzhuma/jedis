package jedis.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Server {
	
	private Selector serverSelector;
	private Map<Client,SocketChannel> clients = new HashMap<>();
	private ServerSocketChannel serverSocketChannel;
	private final int LISTEN_PORT;
	private boolean isStop = false;
	
	public Server(){
		LISTEN_PORT = getListenPort();
	}
	
	private int getListenPort(){
		int port = 8081;
		return port;
	}
	
	public boolean init(){
		try {
			serverSelector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(LISTEN_PORT));
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			System.exit(-1);
		}
		return true;
	}
	
	private void removeClient(SocketChannel clientChannel){
		try {
			String address = clientChannel.getRemoteAddress().toString();
			clients.remove(new Client(address));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void handleAcception(SelectionKey key){
		ServerSocketChannel server = (ServerSocketChannel)key.channel();
		try {
			SocketChannel socketChannel = server.accept();
			socketChannel.configureBlocking(false);
			socketChannel.register(serverSelector,SelectionKey.OP_READ);
			String address = socketChannel.getRemoteAddress().toString();
			clients.put(new Client(address),socketChannel);
			System.out.println("Received Connection From " + address);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private byte[] processCommand(SocketChannel clientChannel,byte[] data){
		return "message received".getBytes();
	}
	
	private boolean sendResponse(SocketChannel clientChannel,
			ByteBuffer responseBuffer,byte[] result){
		int length = result.length;
		if(responseBuffer == null || length > responseBuffer.capacity()){
			responseBuffer = ByteBuffer.allocate(length);
		}else{
			responseBuffer.clear();
		}
		responseBuffer.put(result);
		while(responseBuffer.hasRemaining()){
			try {
				clientChannel.write(responseBuffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	private void handleCommand(SelectionKey key){
		SocketChannel clientChannel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			int size = clientChannel.read(buffer);
			if(size == -1){
				removeClient(clientChannel);
			}else{
				buffer.flip();
				byte[] result = processCommand(clientChannel, buffer.array());
				sendResponse(clientChannel, buffer, result);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			buffer.clear();
		}
	}
	
	private void run(){
		while(!isStop){
			try {
				serverSelector.select();
				Set<SelectionKey> selecedKeys = serverSelector.selectedKeys();
				Iterator<SelectionKey> iterator = selecedKeys.iterator();
				while(iterator.hasNext()){
					SelectionKey key = iterator.next();
					if(key.isAcceptable()){
						handleAcception(key);
					}else if(key.isReadable()){
						handleCommand(key);
					}
				}
				iterator.remove();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		server.init();
		server.run();
	}
	
	class Client{
		private String address;
		public Client(String address) {
			this.address = address;
		}
		
		@Override
		public boolean equals(Object o){
			if(o == this) return true;
			if(!(o instanceof Client)) return false;
			Client client = (Client)o;
			return client.address.equals(this.address);
		}
		
		@Override
		public int hashCode(){
			return this.address.hashCode();
		}
		
		@Override
		public String toString(){
			return this.address;
		}
	}

}
