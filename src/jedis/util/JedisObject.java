package jedis.util;

import java.io.IOException;
import java.io.RandomAccessFile;

public interface JedisObject{
	public byte[] getBytes();
	public JedisObject type();
	public void writeObject(RandomAccessFile file) throws IOException;
	public JedisObject deepCopy();
	public String insertCommand(Sds key);
}
