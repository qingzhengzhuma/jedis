package jedis.util;

public final class MessageConstant {
	public static final int NUMBER_COUNT = 10000;
	public static final JedisObject OK = new Sds("OK");
	public static final JedisObject YES = new Sds("YES");
	public static final JedisObject NO = new Sds("NO");
	public static final JedisObject ERROR = new Sds("ERROR");
	public static final JedisObject PONG = new Sds("PONG");
	public static final JedisObject NOT_SUPPORTED_OPERATION= new Sds("Not Supported Operation By This Type");
	public static final JedisObject ILLEGAL_ARGUMENT = new Sds("Illegal Arguement");
	public static final JedisObject ILLEGAL_COMMAND = new Sds("Illegal Command");
	public static final JedisObject INDEX_OUT_OF_BOUNDARY = new Sds("Index Out Of Boundary");
	public static final JedisObject NO_SUCH_KEY = new Sds("No Such Key");
	public static final JedisObject BYE = new Sds("Bye");
	public static final JedisObject CONNECTION_CLOSED = new Sds("Connection Closed");
	public static final JedisObject AOF_BUSY = new Sds("AOF Serlization Is Running");
	public static final JedisObject[] NUMBERS;
	static{
		NUMBERS = new Sds[NUMBER_COUNT];
		for(int i = 0;i < NUMBER_COUNT;++i){
			NUMBERS[i] = new Sds(Integer.toString(i));
		}
	}
	
}
