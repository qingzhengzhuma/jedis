package jedis.server;

import jedis.util.JedisConfigration;

public class RdbSaveThread extends Thread {
	private JedisDB[] databases;
	public RdbSaveThread(JedisDB[] databases) {
		// TODO Auto-generated constructor stub
		super();
		this.databases = databases;
	}
	
	@Override
	public void run(){
		RDB.save(databases,JedisConfigration.rdbPathName);
	}
}
