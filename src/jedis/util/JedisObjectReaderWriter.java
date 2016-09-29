package jedis.util;

import java.io.IOException;
import java.nio.channels.FileChannel;

public interface JedisObjectReaderWriter {
	public JedisObject readObject(FileChannel channel) throws IOException;
	public void writeObject(FileChannel channel,JedisObject object) throws IOException;
}
