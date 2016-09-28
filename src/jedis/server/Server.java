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

import jedis.util.CommandConfigration;
import jedis.util.CommandHandler;
import jedis.util.CommandLine;
import jedis.util.CommandRule;
import jedis.util.JedisClient;
import jedis.util.JedisDB;
import jedis.util.JedisObject;
import jedis.util.Sds;

public class Server {
	private Selector serverSelector;
	private Map<String, SocketChannel> clientSockets;
	private Map<String, JedisClient> clients;
	private Map<String, CommandHandler> commandTable;
	private ServerSocketChannel serverSocketChannel;

	private int LISTEN_PORT = 8081;
	private boolean isStop = false;
	private int databaseNum = 16;
	private JedisDB[] databases;

	public Server() {

	}

	private void loadConfigration() {

	}

	private void initCommandTable() {
		commandTable = new HashMap<>();
		for (CommandRule ce : CommandConfigration.getCommandRules()) {
			commandTable.put(ce.getCommand(), ce.getHandler());
		}
	}

	public boolean init() {
		try {
			loadConfigration();
			databases = new JedisDB[databaseNum];
			for (int i = 0; i < databaseNum; ++i) {
				databases[i] = new JedisDB();
			}
			clientSockets = new HashMap<>();
			clients = new HashMap<>();
			initCommandTable();
			serverSelector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(LISTEN_PORT));
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			System.exit(-1);
		} catch (Exception e) {
			// TODO: handle exception
			System.exit(-1);
		}
		return true;
	}

	private void removeClient(SelectionKey key) {
		if(key == null) return;
		key.cancel();
		try {
			SocketChannel clientChannel = (SocketChannel) key.channel();
			if(clientChannel != null){
				String address = clientChannel.getRemoteAddress().toString();
				clientSockets.remove(address);
				clients.remove(address);
				clientChannel.close();
				System.out.println(address + "disconnected");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleAcception(SelectionKey key) {
		ServerSocketChannel server = (ServerSocketChannel) key.channel();
		try {
			SocketChannel socketChannel = server.accept();
			if(socketChannel != null){
				socketChannel.configureBlocking(false);
				socketChannel.register(serverSelector, SelectionKey.OP_READ);
				String address = getRemoteAddress(socketChannel);
				clientSockets.put(address, socketChannel);
				clients.put(address, new JedisClient(address));
				System.out.println("Received Connection From " + address);
			}else{
				System.out.println("Connection receive, but failed to Connect.");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getRemoteAddress(SocketChannel socketChannel) {
		try {
			return socketChannel.getRemoteAddress().toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new String();
		}
	}

	private byte[] processCommand(SocketChannel clientChannel, byte[] data) {
		if(CommandLine.parse(new String(data))){
			String command = CommandLine.getCommand().toLowerCase();
			int argc = CommandLine.getArgc();
			if(CommandConfigration.verifyCommand(command,argc) == true){
				String commandLine = CommandLine.getNormalizedCmdLine();
				String address = getRemoteAddress(clientChannel);
				try {
					CommandHandler handler = commandTable.get(command);
					JedisObject object = handler.execute(databases, clients.get(address),
							commandLine);
					if(object == null){
						object = new Sds(("nil"));
					}
					byte[] result = object.getBytes();
					return result;
				} catch (UnsupportedOperationException e) {
					// TODO: handle exception
					return "Not Supported Operation By This Type".getBytes();
				}catch (IllegalArgumentException e) {
					// TODO: handle exception
					return "Illegal Arguement".getBytes();
				}
			}
		}
		return "ILLIGAL COMMAND".getBytes();
	}

	private boolean sendResponse(SocketChannel clientChannel, byte[] result) {
		int length = result.length;
		ByteBuffer buffer = ByteBuffer.allocate(length);
		buffer.put(result);
		buffer.flip();
		while (buffer.hasRemaining()) {
			try {
				clientChannel.write(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
				return false;
			}
		}
		return true;
	}

	private void handleCommand(SelectionKey key) {
		SocketChannel clientChannel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			int size = clientChannel.read(buffer);
			if (size == -1) {
				removeClient(key);
			} else {
				buffer.flip();
				byte[] result = processCommand(clientChannel, buffer.array());
				if(sendResponse(clientChannel, result) == false){
					removeClient(key);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			buffer.clear();
		}
	}

	private void run() {
		while (!isStop) {
			try {
				serverSelector.select();
				Set<SelectionKey> selecedKeys = serverSelector.selectedKeys();
				Iterator<SelectionKey> iterator = selecedKeys.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					if (key.isAcceptable()) {
						handleAcception(key);
					} else if (key.isReadable()) {
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
