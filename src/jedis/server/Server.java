package jedis.server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	public static Map<Sds,Long>[] expireKeys;
	// public static JedisDB[] bufDatabases;
	// public static JedisDB[] deletedDatabases;
	public static RdbSaveThread rdbSaveThread;
	public static AofSaveThread aofSaveThread;
	public static AofState aofState = AofState.AOF_OFF;
	public static AOF aof;
	public static SyncPolicy syncPolicy;
	private int hz = 10;
	public static long cronNums = 0;
	List<TimeEvent> timeEvents;
	long lastSyncTime = System.currentTimeMillis();
	public static final long aofFileSizeThreshold = 1024 * 1024 * 256; // 256MB

	private Server() {
		timeEvents = new LinkedList<>();
		timeEvents.add(new ServerTimeEvent(System.currentTimeMillis()));
	}

	private TimeEvent getNearestTimeEvent() {
		TimeEvent event = null;
		long now = System.currentTimeMillis();
		long minWaitVal = Long.MAX_VALUE;
		for (TimeEvent e : timeEvents) {
			if (e.when - now < minWaitVal) {
				event = e;
				minWaitVal = e.when - now;
			}
		}
		return event;
	}

	private void loadConfigration() {
		aofState = AofState.AOF_OFF;
		syncPolicy = SyncPolicy.EVERY_SECOND;
	}

	@SuppressWarnings("unchecked")
	public static void initDatabases(int dbNum) {
		inUseDatabases = new JedisDB[dbNum];
		expireKeys = (Map<Sds,Long>[])new HashMap[dbNum];
		for (int i = 0; i < dbNum; ++i) {
			inUseDatabases[i] = new JedisDB();
			expireKeys[i] = new HashMap<>();
		}
		
	}

	public boolean init() {
		try {
			loadConfigration();
			clientSockets = new HashMap<>();
			clients = new HashMap<>();
			serverSelector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(LISTEN_PORT));
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
			loadDatabase();
			if (aofState == AofState.AOF_ON) {
				aof = AOF.openForAppend(JedisConfigration.aofPathName);
				aof.setAofState(aofState);
				aof.setSyncPolicy(syncPolicy);
			}
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					if (aofState == AofState.AOF_ON) {
						aof.close();
					}
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
		String address = getRemoteAddress(clientChannel);
		JedisClient client = clients.get(address);
		return CommandHandler.executeCommand(client, data).getBytes();
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
		if (aofState == AofState.AOF_ON) {
			AOF.load(JedisConfigration.aofPathName);
		} else if (new File(JedisConfigration.rdbPathName).exists()) {
			RDB.load(JedisConfigration.rdbPathName);
		} else {
			initDatabases(databaseNum);
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
	
	public static boolean isKeyExpired(Sds key,int dbIndex){
		Long expireTime = null;
		if(dbIndex >= 0 && dbIndex < databaseNum &&
				(expireTime = expireKeys[dbIndex].get(key)) != null &&
				System.currentTimeMillis() >= expireTime){
			return true;
		}
		return false;
	}
	
	public static void removeIfExpired(Sds key,int dbIndex){
		if(isKeyExpired(key, dbIndex)){
			removeExpiredKey(key, dbIndex);
		}
	}
	
	public static void removeExpiredKey(Sds key,int dbIndex){
		inUseDatabases[dbIndex].remove(key);
		expireKeys[dbIndex].remove(key);
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

	private void processTimeEvent(TimeEvent event) {
		long now = System.currentTimeMillis();
		if (event != null && event.when <= now) {
			long t = event.process();
			if (t > 0) {
				event.when += t;
			} else {
				timeEvents.remove(event);
			}
		}
	}

	private void run() {
		try {
			while (!isStop) {
				TimeEvent event = getNearestTimeEvent();
				long latestExpiredTime = event == null ? Long.MAX_VALUE : event.when;
				long waitTime = latestExpiredTime - System.currentTimeMillis();
				if (waitTime < 0) waitTime = 0;
				serverSelector.select(waitTime);
				processFileEvent(serverSelector);
				processTimeEvent(event);
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

	class ServerTimeEvent extends TimeEvent {
		private int lastExpiredDbIndex = 0;
		ServerTimeEvent(long when) {
			// TODO Auto-generated constructor stub
			super(when);
		}
		
		private void processAOF() {
			if (aofState == AofState.AOF_ON && aof != null) {
				if (syncPolicy == SyncPolicy.EVERY_SECOND) {
					long now = System.currentTimeMillis();
					if (now - lastSyncTime >= 1000) {
						aof.fsync();
						lastSyncTime = now;
					}
				}
				try {
					if (aof.size() >= aofFileSizeThreshold) {
						aof.close();
						AofSaveThread t = new AofSaveThread();
						t.start();
						t.join();
						aof = AOF.openForAppend(JedisConfigration.aofPathName);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		private void processExpireKeys(){
			List<Sds> expiredKeys = new ArrayList<>();
			for(Entry<Sds, Long> entry : expireKeys[lastExpiredDbIndex].entrySet()){
				Sds key = entry.getKey();
				if(isKeyExpired(entry.getKey(), lastExpiredDbIndex)){
					expiredKeys.add(key);
				}
			}
			for(Sds key : expiredKeys){
				removeExpiredKey(key, lastExpiredDbIndex);
			}
			lastExpiredDbIndex = (lastExpiredDbIndex + 1) % databaseNum;
		}

		@Override
		public long process() {
			processAOF();
			processExpireKeys();
			++cronNums;
			return 1000 / hz;
		}
	}
}
