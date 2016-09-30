package jedis.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import jedis.util.JedisConfigration;
import jedis.util.JedisObject;
import jedis.util.Sds;

public class RDB {
	public static void load(String rdbFilePath) throws IOException {
		int dbNum = 0;
		RandomAccessFile rdbFile;
		rdbFile = new RandomAccessFile(rdbFilePath, "r");
		JedisDB[] databases = Server.inUseDatabases;
		dbNum = rdbFile.readInt();
		databases = new JedisDB[dbNum];
		SdsReaderWriter sdsReaderWriter = new SdsReaderWriter();
		for (int i = 0; i < dbNum; ++i) {
			databases[i] = new JedisDB();
			@SuppressWarnings("unused")
			int dbIndex = rdbFile.readInt(); //
			int kvPairNum = rdbFile.readInt();
			for (int j = 0; j < kvPairNum; ++j) {
				rdbFile.readByte(); // skip the key type code, key is always Sds
									// type
				Sds key = (Sds) sdsReaderWriter.readObject(rdbFile);
				rdbFile.readByte(); // skip the value type, currently value is
									// always Sds type
				JedisObject value = sdsReaderWriter.readObject(rdbFile);
				databases[i].set(key, value);
			}
		}
		rdbFile.close();
	}

	public static void save(JedisDB[] databases, String rdbPathName) {
		String fileName = Long.toString(new Date().getTime())
				+ Integer.toString((int) new Random(System.currentTimeMillis()).nextInt(100000));
		try {
			String tempPath = JedisConfigration.workPath + fileName;
			RandomAccessFile file = new RandomAccessFile(tempPath, "rw");
			file.writeInt(databases.length);
			int dbIndex = 0;
			for (JedisDB db : databases) {
				file.writeInt(dbIndex++);
				Set<Entry<Sds, JedisObject>> entries = db.getDict().entrySet();
				file.writeInt(entries.size());
				for (Entry<Sds, JedisObject> entry : entries) {
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
			File newFile = new File(rdbPathName);
			if (newFile.exists())
				newFile.delete();
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
