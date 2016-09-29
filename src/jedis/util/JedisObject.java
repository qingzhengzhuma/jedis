package jedis.util;

import java.io.IOException;
import java.nio.channels.FileChannel;

public interface JedisObject {
	public byte[] getBytes();
	public JedisObject type();
	public void writeObject(FileChannel channel) throws IOException;
}
