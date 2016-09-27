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

import jedis.util.JedisDB;
import jedis.util.CommandHandler;
import jedis.util.JedisClient;

public class Server {
	
	private Selector serverSelector;
	private Map<String,SocketChannel> clientSockets;
	private Map<String, JedisClient> clients;
	private Map<String, CommandHandler> commandTable;
	private ServerSocketChannel serverSocketChannel;
	private int LISTEN_PORT = 8081;
	private boolean isStop = false;
	private int databaseNum = 16;
	private JedisDB[] databases;
	public Server(){
		
	}
	
	private int loadConfigration(){
		int port = 8081;
		return port;
	}
	
	public boolean init(){
		try {
			loadConfigration();
			databases = new JedisDB[databaseNum];
			clientSockets = new HashMap<>();
			commandTable = new HashMap<>();
			serverSelector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(LISTEN_PORT));
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			System.exit(-1);
		}catch (Exception e) {
			// TODO: handle exception
			System.exit(-1);
		}
		return true;
	}
	
	private void removeClient(SocketChannel clientChannel){
		try {
			String address = clientChannel.getRemoteAddress().toString();
			clientSockets.remove(address);
			clients.remove(address);
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
			String address = getRemoteAddress(socketChannel);
			clientSockets.put(address,socketChannel);
			clients.put(address, new JedisClient(address));
			System.out.println("Received Connection From " + address);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getRemoteAddress(SocketChannel socketChannel){
		try {
			return socketChannel.getRemoteAddress().toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new String();
		}
	}
	
	private String parseCommand(byte[] data){
		int length = data == null ? 0 : data.length;
		int i = 0;
		while(i < length && Character.isWhitespace(data[i++]));
		StringBuilder stringBuilder = new StringBuilder();
		while(i < length && Character.isWhitespace(data[i])) {
			stringBuilder.append(data[i++]);
		}
		return stringBuilder.toString();
	}
	
	private boolean isValidCommand(String command){
		return command != null && 
				command.length() > 0 && 
				commandTable.containsKey(command);
	}
	
	private byte[] processCommand(SocketChannel clientChannel,byte[] data){
		String command = parseCommand(data);
		if(!isValidCommand(command)) return "Unkonwn Command".getBytes();
		String address = getRemoteAddress(clientChannel);
		if(clientSockets.containsKey(address) && clients.containsKey(address)){
			CommandHandler handler = commandTable.get(command);
			return handler.execute(databases,
					clients.get(address),
					new String(data)).getBytes();
		}
		return new byte[0];
	}
	
	private boolean sendResponse(SocketChannel clientChannel,byte[] result){
		int length = result.length;
		ByteBuffer buffer = ByteBuffer.allocate(length);
		buffer.put(result);
		buffer.flip();
		while(buffer.hasRemaining()){
			try {
				clientChannel.write(buffer);
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
				System.out.println(new String(result));
				sendResponse(clientChannel,result);
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
}
