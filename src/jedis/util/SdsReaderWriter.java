package jedis.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.rmi.UnexpectedException;

public class SdsReaderWriter implements JedisObjectReaderWriter {

	@Override
	public JedisObject readObject(FileChannel channel) 
			throws IOException {
		// TODO Auto-generated method stub
		ByteBuffer buffer = ByteBuffer.allocate(4);
		channel.read(buffer);
		buffer.flip();
		int length = buffer.getInt();
		buffer = ByteBuffer.allocate(length);
		int left = length;
		while(left != 0){
			int size = channel.read(buffer);
			if(size == -1){
				throw new UnexpectedException("Unexpected End of File");
			}
			left -= size;
		}
		buffer.flip();
		return new Sds(buffer.array());
	}
	
	@Override
	public void writeObject(FileChannel channel,
			JedisObject object) throws IOException{
		object.writeObject(channel);
	}

}
