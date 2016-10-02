package jedis.server;

abstract class TimeEvent {
	long when;
	public TimeEvent(long when){
		this.when = when;
	}
	
	abstract public long process();
}
