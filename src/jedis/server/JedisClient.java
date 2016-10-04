package jedis.server;

import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import jedis.util.CommandLine;
import jedis.util.Sds;

public class JedisClient{
	String address;
	JedisDB db;
	boolean dirtyCas;
	SocketChannel channel;
	MultiState multiState;
	Set<Sds> watchedKeys;
	Queue<CommandLine> multiCommandBuf;
	
	public JedisClient(String address,SocketChannel channel) {
		this.address = address;
		this.channel = channel;
		this.db = Server.inUseDatabases[0];
		this.dirtyCas = false;
		this.multiState = MultiState.NONE;
		this.watchedKeys = new HashSet<>();
		multiCommandBuf = new LinkedList<>();
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this) return true;
		if(!(o instanceof JedisClient)) return false;
		JedisClient client = (JedisClient)o;
		return client.address.equals(this.address);
	}
	
	public void clearWatch(){
		for(Sds key : watchedKeys){
			List<JedisClient> clients = db.watchedKeys.get(key);
			if(clients != null){
				clients.remove(this);
			}
		}
		watchedKeys.clear();
		dirtyCas = false;
	}
	
	public void watch(CommandLine cl){
		int argc = cl.getArgc();
		for (int i = 0; i < argc; ++i) {
			Sds key = new Sds(cl.getArg(i));
			db.removeIfExpired(key);
			if(watchedKeys.contains(key)) continue;
			List<JedisClient> clns = db.watchedKeys.get(key);
			if (clns == null) {
				clns = new LinkedList<>();
				db.watchedKeys.put(key, clns);
			}
			watchedKeys.add(key);
			clns.add(this);
		}
	}
	
	@Override
	public int hashCode(){
		int hash = 23;
		hash = 37*hash + this.address.hashCode();
		return hash;
	}
	
	@Override
	public String toString(){
		return this.address;
	}
}