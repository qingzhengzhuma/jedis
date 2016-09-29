package jedis.server;

import java.io.IOException;
import java.io.RandomAccessFile;

import jedis.util.JedisObject;
import jedis.util.Sds;

public class SdsReaderWriter implements JedisObjectReaderWriter {

	@Override
	public JedisObject readObject(RandomAccessFile file) 
			throws IOException {
		// TODO Auto-generated method stub
		int length = file.readInt();
		byte[] buf = new byte[length];
		//System.out.println(length);
		file.read(buf);
		return new Sds(buf);
	}
	
	@Override
	public void writeObject(RandomAccessFile file,
			JedisObject object) throws IOException{
		object.writeObject(file);
	}

}
