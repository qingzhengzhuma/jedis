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
	
	private Selector connectionSelector;
	private Selector dataSelector;
	private Map<Client,SocketChannel> clients = new HashMap<>();
	private ServerSocketChannel serverSocketChannel;
	private final int LISTEN_PORT;
	private boolean isStop = false;
	private Object lock = new Object();
	
	public Server(){
		LISTEN_PORT = getListenPort();
	}
	
	private int getListenPort(){
		int port = 8081;
		return port;
	}
	
	public boolean init(){
		try {
			connectionSelector = Selector.open();
			dataSelector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(LISTEN_PORT));
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.register(connectionSelector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			System.exit(-1);
		}
		return true;
	}
	
	private void removeClient(SocketChannel clientChannel){
		try {
			String IP = clientChannel.getLocalAddress().toString();
			int port = clientChannel.socket().getLocalPort();
			clients.remove(new Client(IP, port));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void handleAcception(SelectionKey key){
		ServerSocketChannel server = (ServerSocketChannel)key.channel();
		try {
			SocketChannel socketChannel = server.accept();
			socketChannel.configureBlocking(false);
			synchronized (lock) {
				dataSelector.wakeup();
				socketChannel.register(dataSelector,SelectionKey.OP_READ);
				String IP = socketChannel.getLocalAddress().toString();
				int port = socketChannel.socket().getLocalPort();
				clients.put(new Client(IP, port),socketChannel);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void processData(SocketChannel clientChannel,byte[] data){
		ByteBuffer buffer = ByteBuffer.allocate(20);
		buffer.put("received".getBytes());
		while(buffer.hasRemaining()){
			try {
				clientChannel.write(buffer);
			} catch (IOException e) {
				e.printStackTrace();
				removeClient(clientChannel);
			}
		}
	}
	
	private void handleData(SelectionKey key){
		SocketChannel dataChannel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			String IP = dataChannel.getLocalAddress().toString();
			int port = dataChannel.socket().getLocalPort();
			int size = dataChannel.read(buffer);
			if(size == -1){
				clients.remove(new Client(IP, port));
			}else{
				buffer.flip();
				System.out.println("FROM " + IP + ":" + Integer.toString(port)
				+ "##" + new String(buffer.array()));
				processData(dataChannel, buffer.array());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			buffer.clear();
		}
	}
	
	private void handleConnectionEvent(){
		System.out.println("listening...");
		while(!isStop){
			try {
				connectionSelector.select();
				Set<SelectionKey> selecedKeys = connectionSelector.selectedKeys();
				Iterator<SelectionKey> iterator = selecedKeys.iterator();
				while(iterator.hasNext()){
					SelectionKey key = iterator.next();
					if(key.isAcceptable()){
						handleAcception(key);
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
		DataHandleThread thread = server.new DataHandleThread();
		thread.start();
		server.handleConnectionEvent();
		thread.tryStop();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	class DataHandleThread extends Thread{
		private boolean isStop = false;
		@Override
		public void run(){
			System.out.println("Waiting for data's coming...");
			while(!isStop){
				synchronized (lock) {
					try {
						dataSelector.select();
						Set<SelectionKey> selectedKeys = dataSelector.selectedKeys();
						Iterator<SelectionKey> iterator = selectedKeys.iterator();
						while(iterator.hasNext()){
							SelectionKey key = iterator.next();
							if(key.isReadable()){
								handleData(key);
							}
							iterator.remove();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		public void tryStop() {
			isStop = true;
		}
	}
	
	class Client{
		private String IP;
		private int port;
		public Client(String IP,int port) {
			this.IP = IP;
			this.port = port;
		}
		
		@Override
		public boolean equals(Object o){
			if(o == this) return true;
			if(!(o instanceof Client)) return false;
			Client client = (Client)o;
			return client.IP.equals(this.IP) && client.port == this.port;
		}
		
		@Override
		public int hashCode(){
			int hash = 23;
			hash = hash*37 + this.IP.hashCode();
			hash = hash*37 + this.port;
			return hash;
		}
		
		@Override
		public String toString(){
			return this.IP + ":" + Integer.toString(this.port);
		}
	}

}
