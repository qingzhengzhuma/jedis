package jedis.util;

public class JedisClient{
	private String address;
	private int currentDB;
	
	public JedisClient(String address) {
		this.currentDB = 0;
		this.address = address;
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this) return true;
		if(!(o instanceof JedisClient)) return false;
		JedisClient client = (JedisClient)o;
		return client.currentDB == this. currentDB &&
				client.address.equals(this.address);
	}
	
	@Override
	public int hashCode(){
		int hash = 23;
		hash = 37*hash + this.currentDB;
		hash = 37*hash + this.address.hashCode();
		return hash;
	}
	
	@Override
	public String toString(){
		return this.address + ":" + Integer.toString(this.currentDB);
	}
	
	public void setCurrentDB(int newDB){
		this.currentDB = newDB;
	}
	
	public int getCurrntDB(){
		return this.currentDB;
	}
}