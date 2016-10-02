package jedis.server;

class TimeEvent {
	long excuteTime;
	TimeEventHandler handler;
	TimeEventType type;
	public TimeEvent(long excuteTime,TimeEventHandler handler,TimeEventType type){
		this.excuteTime = excuteTime;
		this.handler = handler;
		this.type = type;
	}
	
	public void process(){
		if(handler != null){
			handler.process();
		}
	}
}
