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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jedis.util.CommandLine;
import jedis.util.JedisObject;
import jedis.util.Sds;

public class Server {
	private Selector serverSelector;
	private Map<String, JedisClient> clients;
	private ServerSocketChannel serverSocketChannel;

	private int LISTEN_PORT = 8081;
	private boolean isStop = false;
	private static int databaseNum = 16;
	public static JedisDB[] inUseDatabases;
	public static RdbSaveThread rdbSaveThread;
	public static AofSaveThread aofSaveThread;
	public static AofState aofState = AofState.AOF_OFF;
	public static AOF aof;
	public static SyncPolicy syncPolicy;
	private int hz = 10;
	public static long cronNums = 0;
	List<TimeEvent> timeEvents;
	long lastSyncTime = System.currentTimeMillis();
	static final long aofFileSizeThreshold = 1024 * 1024 * 256; // 256MB
	static Map<Sds, List<JedisClient>> subscribedChannels;
	static Set<JedisClient> monitors;

	private Server() {
		clients = new HashMap<>();
		timeEvents = new LinkedList<>();
		timeEvents.add(new ServerTimeEvent(System.currentTimeMillis()));
		subscribedChannels = new HashMap<>();
		monitors = new HashSet<>();
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

	static void initDatabases(int dbNum) {
		inUseDatabases = new JedisDB[dbNum];
		for (int i = 0; i < dbNum; ++i) {
			inUseDatabases[i] = new JedisDB();
		}

	}

	public boolean init() {
		try {
			loadConfigration();
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
			e.printStackTrace();
			System.exit(-1);
		}
		return true;
	}

	private void removeClient(JedisClient client) {
		if (client != null) {
			try {
				String address = client.address;
				clients.remove(address);
				if (client != null) {
					for (Sds channel : client.subscriedChannel) {
						List<JedisClient> clns = Server.subscribedChannels.get(channel);
						if (clns != null) {
							clns.remove(client);
						}
					}
					monitors.remove(client);
				}
				client.channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String getRemoteAddress(SocketChannel socketChannel) {
		try {
			return socketChannel.getRemoteAddress().toString();
		} catch (IOException e) {
			e.printStackTrace();
			return new String();
		}
	}
	
	void sendMsgToMonitor(Sds msg){
		for(JedisClient client : monitors){
			client.pushResult(msg);
			client.sendResponse();
		}
	}
	
	boolean executeCommand(JedisClient client, byte[] data) {
		CommandLine cl = new CommandLine();
		boolean state = false;
		if (cl.parse(new String(data))) {
			String command = cl.getNormalizedCmd();
			int argc = cl.getArgc();
			if (CommandHandler.verifyCommand(command, argc) == true) {
				try {
					Sds msg = new Sds(cl.getCmdLine());
					sendMsgToMonitor(msg);
					CommandHandler handler = CommandHandler.getHandler(client, command);
					JedisObject object = handler.execute(client, cl);
					if (object == null)
						object = MessageConstant.NIL;
					state = true;
					client.pushResult(object);
				} catch (UnsupportedOperationException e) {
					// TODO: handle exception
					client.pushResult(MessageConstant.NOT_SUPPORTED_OPERATION);
				} catch (IllegalArgumentException e) {
					// TODO: handle exception
					client.pushResult(MessageConstant.ILLEGAL_ARGUMENT);
				}
			}else{
				client.pushResult(MessageConstant.ILLEGAL_COMMAND);
			}
		}else{
			client.pushResult(MessageConstant.ILLEGAL_COMMAND);
		}
		return state;
	}
    
	private boolean handleCommand(JedisClient client) {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			int size = client.channel.read(buffer);
			if (size == -1) {
				removeClient(client);
				System.out.println(client.address + " " + MessageConstant.CONNECTION_CLOSED);
				return false;
			} else {
				buffer.flip();
				if(buffer.hasArray()){
					byte[] data = buffer.array();
					boolean state = executeCommand(client,data);
					if(state == true){
						for(JedisClient c : monitors){
							c.pushResult(new Sds(data));
							if(!c.sendResponse()){
								removeClient(c);
							}
						}
					}
					if (client.sendResponse() == false) {
						removeClient(client);
						System.out.println(client.address + " " + MessageConstant.CONNECTION_CLOSED);
						return false;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			buffer.clear();
		}
		return true;
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
				clients.put(address, new JedisClient(address, socketChannel));
				System.out.println("Received Connection From " + address);
			} else {
				System.out.println("Connection receive, but failed to Connect.");
			}

		} catch (IOException e) {
			e.printStackTrace();
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
				SocketChannel clientChannel = (SocketChannel) key.channel();
				String address = getRemoteAddress(clientChannel);
				JedisClient client = clients.get(address);
				if(!handleCommand(client)){
					key.cancel();
				}
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
				if (waitTime < 0)
					waitTime = 0;
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

		private void processExpireKeys() {
			List<Sds> expiredKeys = new ArrayList<>();
			JedisDB db = Server.inUseDatabases[lastExpiredDbIndex];
			for (Entry<Sds, Long> entry : db.expireKeys.entrySet()) {
				Sds key = entry.getKey();
				if (db.isKeyExpired(key)) {
					expiredKeys.add(key);
				}
			}
			for (Sds key : expiredKeys) {
				db.removeExpiredKey(key);
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
