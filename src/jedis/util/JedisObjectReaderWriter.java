package jedis.util;

import java.io.IOException;
import java.io.RandomAccessFile;

public interface JedisObjectReaderWriter {
	public JedisObject readObject(RandomAccessFile file) throws IOException;
	public void writeObject(RandomAccessFile file,JedisObject object) throws IOException;
}
