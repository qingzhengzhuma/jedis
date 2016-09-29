package jedis.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

public class RdbSaveThread extends Thread {
	private JedisDB[] databases;
	public RdbSaveThread(JedisDB[] databases) {
		// TODO Auto-generated constructor stub
		super();
		this.databases = databases;
	}
	
	private void writeInt(FileChannel channel,ByteBuffer buffer,int n) throws IOException{
		buffer.putInt(n);
		buffer.flip();
		channel.write(buffer);
		buffer.clear();
	}
	
	@Override
	public void run(){
		String fileName = Long.toString(new Date().getTime())
				+ Integer.toString((int)new Random(System.currentTimeMillis()).nextInt(100000));
		try {
			RandomAccessFile file = new RandomAccessFile(fileName, "w");
			FileChannel channel = file.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(16);
			writeInt(channel, buffer, databases.length);
			int dbIndex = 0;
			for(JedisDB db : databases){
				writeInt(channel, buffer, dbIndex++);
				Set<Entry<Sds, JedisObject>> entries = db.getDict().entrySet();
				writeInt(channel, buffer, entries.size());
				for(Entry<Sds, JedisObject> entry : entries){
					JedisObject key = entry.getKey();
					JedisObject value = entry.getValue();
					try {
						key.writeObject(channel);
						value.writeObject(channel);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			channel.close();
			file.close();
			File oldFile = new File(JedisConfigration.workPath + fileName);
			File newFile = new File(JedisConfigration.workPath + JedisConfigration.rdbFileName);
			oldFile.renameTo(newFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
