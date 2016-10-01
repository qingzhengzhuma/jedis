package jedis.server;

import java.io.File;
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

import jedis.util.CommandLine;
import jedis.util.JedisConfigration;
import jedis.util.JedisObject;
import jedis.util.MessageConstant;
import jedis.util.Sds;

public class Server {
	private Selector serverSelector;
	private Map<String, SocketChannel> clientSockets;
	private Map<String, JedisClient> clients;
	private ServerSocketChannel serverSocketChannel;

	private int LISTEN_PORT = 8081;
	private boolean isStop = false;
	private static int databaseNum = 16;
	public static JedisDB[] inUseDatabases;
	//public static JedisDB[] bufDatabases;
	//public static JedisDB[] deletedDatabases;
	public static RdbSaveThread rdbSaveThread;
	public static AofSaveThread aofSaveThread;
	public static AofState aofState = AofState.AOF_OFF;
	public static AOF aof;
	public static SyncPolicy syncPolicy;
	private int hz = 10;
	private long lastSyncTime = 0;
	public static long cronNums = 0;

	private Server() {
		
	}

	private void loadConfigration() {
		aofState = AofState.AOF_ON;
		syncPolicy = SyncPolicy.EVERY_SECOND;
	}

	public static JedisDB[] initDatabases(int dbNum) {
		JedisDB[] dbs = new JedisDB[dbNum];
		for (int i = 0; i < dbNum; ++i) {
			dbs[i] = new JedisDB();
		}
		return dbs;
	}

	public boolean init() {
		try {
			loadConfigration();
			inUseDatabases = initDatabases(databaseNum);
			clientSockets = new HashMap<>();
			clients = new HashMap<>();
			serverSelector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(LISTEN_PORT));
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
			aof = AOF.openForAppend(JedisConfigration.aofPathName);
			loadDatabase();
			aof = AOF.openForAppend(JedisConfigration.aofPathName);
			aof.setAofState(aofState);
			aof.setSyncPolicy(syncPolicy);
			Runtime.getRuntime().addShutdownHook(new Thread(){
				@Override
				public void run(){
					aof.close();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.exit(-1);
		}
		return true;
	}

	private void removeClient(SelectionKey key) {
		if (key == null)
			return;
		key.cancel();
		try {
			SocketChannel clientChannel = (SocketChannel) key.channel();
			if (clientChannel != null) {
				String address = clientChannel.getRemoteAddress().toString();
				clientSockets.remove(address);
				clients.remove(address);
				clientChannel.close();
				System.out.println(address + " " + MessageConstant.CONNECTION_CLOSED);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleAcception(SelectionKey key) {
		ServerSocketChannel server = (ServerSocketChannel) key.channel();
		try {
			SocketChannel socketChannel = server.accept();
			if (socketChannel != null) {
				socketChannel.configureBlocking(false);
				socketChannel.register(serverSelector, SelectionKey.OP_READ);
				String address = getRemoteAddress(socketChannel);
				clientSockets.put(address, socketChannel);
				clients.put(address, new JedisClient(address));
				System.out.println("Received Connection From " + address);
			} else {
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
		CommandLine cl = new CommandLine();
		if (cl.parse(new String(data))) {
			String command = cl.getNormalizedCmd();
			int argc = cl.getArgc();
			if (JedisConfigration.verifyCommand(command, argc) == true) {
				String address = getRemoteAddress(clientChannel);
				try {
					CommandHandler handler = JedisConfigration.getHandler(command);
					JedisObject object = handler.execute(clients.get(address), cl);
					if (object == null) {
						object = new Sds(("nil"));
					}
					byte[] result = object.getBytes();
					return result;
				} catch (UnsupportedOperationException e) {
					// TODO: handle exception
					return MessageConstant.NOT_SUPPORTED_OPERATION.getBytes();
				} catch (IllegalArgumentException e) {
					// TODO: handle exception
					return MessageConstant.ILLEGAL_ARGUMENT.getBytes();
				}
			}
		}
		return MessageConstant.ILLEGAL_COMMAND.getBytes();
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
				if (sendResponse(clientChannel, result) == false) {
					removeClient(key);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			buffer.clear();
		}
	}

	private void loadDatabase() throws IOException {
		if (new File(JedisConfigration.aofPathName).exists()) {
			AOF.load(JedisConfigration.aofPathName);
		} else if (new File(JedisConfigration.rdbPathName).exists()) {
			RDB.load(JedisConfigration.rdbPathName);
		} else {
			inUseDatabases = initDatabases(databaseNum);
		}
	}

	private void processFileEvent(Selector selector) {
		Set<SelectionKey> selecedKeys = selector.selectedKeys();
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
	}

	private long getLatestExpireTime() {
		return 600;
	}

	private void run() {
		long frequency = 1000 / hz;
		try {
			while (!isStop) {
				long latestExpiredTime = getLatestExpireTime();
				long waitTime = Math.min(frequency, latestExpiredTime);
				int keyNum = serverSelector.select(waitTime);
				if (keyNum > 0) {
					processFileEvent(serverSelector);
				}
				long t = System.currentTimeMillis();
				if (t -   latestExpiredTime >= 0) {
					//processing expired keys
				}
				if (aofState == AofState.AOF_ON && syncPolicy == SyncPolicy.EVERY_SECOND
						&& (t - lastSyncTime) >= 1000) {
					lastSyncTime = t;
					aof.fsync();
				}
				++cronNums;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.init();
		server.run();
	}
}
