package jedis.server;

public class JedisConfigration {
	
	public static JedisObjectReaderWriter[] getObjectReaders(){
		JedisObjectReaderWriter[]  readers = {
				new SdsReaderWriter(),
		};
		return readers;
	}
	
	public static final String workPath = "/home/liaojian/workspace/";
	public static final String rdbPathName = workPath + "jedis.rdb";
	public static final String aofPathName = workPath + "jedis.aof";
}

