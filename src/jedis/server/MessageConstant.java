package jedis.server;

import jedis.util.JedisObject;
import jedis.util.Sds;

public final class MessageConstant {
	public static final int NUMBER_COUNT = 10000;
	public static final Sds OK = new Sds("OK");
	public static final Sds YES = new Sds("YES");
	public static final Sds NO = new Sds("NO");
	public static final Sds ERROR = new Sds("ERROR");
	public static final Sds PONG = new Sds("PONG");
	public static final Sds NOT_SUPPORTED_OPERATION= new Sds("Not Supported Operation By This Type");
	public static final Sds ILLEGAL_ARGUMENT = new Sds("Illegal Arguement");
	public static final Sds ILLEGAL_COMMAND = new Sds("Illegal Command");
	public static final Sds INDEX_OUT_OF_BOUNDARY = new Sds("Index Out Of Boundary");
	public static final Sds NO_SUCH_KEY = new Sds("No Such Key");
	public static final Sds BYE = new Sds("Bye");
	public static final Sds CONNECTION_CLOSED = new Sds("Connection Closed");
	public static final Sds AOF_BUSY = new Sds("AOF Serlization Is Running");
	public static final Sds RDB_BUSY = new Sds("RDB Serlization Is Running");
	public static final Sds NIL = new Sds("nil");
	public static final Sds MULTI_CANNOT_BE_NEST = new Sds("Multi can't be nested");
	public static final Sds EXEC_WITHOUT_MULTI = new Sds("Exec without multi");
	public static final Sds ERROR_HAPPED_IN_TRANSACTIOIN = new Sds("Transaction discarded because of previous errors");
	public static final Sds MULTI_COMMAND_EMPTY = new Sds("Empty list or set");
	public static final Sds QUEUED = new Sds("QUEUED");
	public static final Sds WATCH_INSIDE_MULTI_NOT_ALLOWED = new Sds("Watch inside multi is not allowed");
	public static final Sds[] NUMBERS;
	static{
		NUMBERS = new Sds[NUMBER_COUNT];
		for(int i = 0;i < NUMBER_COUNT;++i){
			NUMBERS[i] = new Sds(Integer.toString(i));
		}
	}
	
}
