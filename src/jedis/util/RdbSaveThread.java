package jedis.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

public class RdbSaveThread extends Thread {
	private JedisDB[] databases;
	public RdbSaveThread(JedisDB[] databases) {
		// TODO Auto-generated constructor stub
		super();
		this.databases = databases;
	}
	
	@Override
	public void run(){
		String fileName = Long.toString(new Date().getTime())
				+ Integer.toString((int)new Random(System.currentTimeMillis()).nextInt(100000));
		try {
			String tempPath = JedisConfigration.workPath + fileName;
			RandomAccessFile file = new RandomAccessFile(tempPath, "rw");
			file.writeInt(databases.length);
			int dbIndex = 0;
			for(JedisDB db : databases){
				file.writeInt(dbIndex++);
				Set<Entry<Sds, JedisObject>> entries = db.getDict().entrySet();
				file.writeInt(entries.size());
				for(Entry<Sds, JedisObject> entry : entries){
					JedisObject key = entry.getKey();
					JedisObject value = entry.getValue();
					try {
						key.writeObject(file);
						value.writeObject(file);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			file.close();
			File oldFile = new File(tempPath);
			File newFile = new File(JedisConfigration.workPath + JedisConfigration.rdbFileName);
			if(newFile.exists()) newFile.delete();
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
