package jedis.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RdbLoader {
	public static JedisDB[] load(String rdbFilePath) throws IOException{
		int dbNum = 0;
		JedisDB[] databases = new JedisDB[dbNum];
		RandomAccessFile rdbFile;
		try {
			rdbFile = new RandomAccessFile(rdbFilePath, "r");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return databases;
		}
		try {
			dbNum = rdbFile.readInt();
			databases = new JedisDB[dbNum];
			SdsReaderWriter sdsReaderWriter = new SdsReaderWriter();
			for(int i = 0; i < dbNum;++i){
				databases[i] = new JedisDB();
				@SuppressWarnings("unused")
				int dbIndex = rdbFile.readInt(); //
				int kvPairNum = rdbFile.readInt();
				for(int j = 0; j < kvPairNum;++j){
					rdbFile.readByte(); //skip the key type code, key is always Sds type
					Sds key = (Sds)sdsReaderWriter.readObject(rdbFile);
					rdbFile.readByte(); //skip the value type, currently value is always Sds type
					JedisObject value = sdsReaderWriter.readObject(rdbFile);
					databases[i].set(key, value);
				}
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if(rdbFile != null){
				rdbFile.close();
			}
		}
		return databases;
	}
}
