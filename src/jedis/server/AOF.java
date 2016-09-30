package jedis.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.Random;

import jedis.util.CommandLine;
import jedis.util.JedisConfigration;
import jedis.util.JedisObject;
import jedis.util.Sds;

public class AOF {
	RandomAccessFile aofFile;
	SyncPolicy syncPolicy = SyncPolicy.EVERY_SECOND;
	AofState state = AofState.AOF_OFF;
	private AOF(RandomAccessFile aofFile) {
		this.aofFile = aofFile;
	}

	public static AOF openForRead(String filePathName) throws FileNotFoundException {
		RandomAccessFile file = new RandomAccessFile(filePathName, "r");
		return new AOF(file);
	}

	public static AOF openForAppend(String filePathName) throws IOException {
		RandomAccessFile file = new RandomAccessFile(filePathName, "rw");
		file.seek(file.length());
		return new AOF(file);
	}
	
	public void setSyncPolicy(SyncPolicy policy){
		this.syncPolicy = policy;
	}
	
	public void setAofState(AofState state){
		this.state = state;
	}
	
	public void fsync(){
		try {
			aofFile.getChannel().force(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			aofFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void put(CommandLine cmd,int dbIndex){
		if(state == AofState.AOF_ON) {
			try {
				aofFile.writeInt(dbIndex);
				aofFile.writeBytes(cmd.getNormalizedCmdLine());
				aofFile.writeBytes("\r\n");
				if(this.syncPolicy == SyncPolicy.ALWAYS){
					fsync();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String next() throws IOException {
		return aofFile.readLine();
	}
	
	public void rewind() throws IOException{
		aofFile.seek(0);
	}

	public static boolean save(JedisDB[] databases, String aofPathName) throws IOException {
		String fileName = Long.toString(new Date().getTime())
				+ Integer.toString((int)new Random(System.currentTimeMillis()).nextInt(100000));
		RandomAccessFile aFile;
		aFile = new RandomAccessFile(fileName, "rw");
		int dbNum = databases == null ? 0 : databases.length;
		for (int i = 0; i < dbNum; ++i) {
			if(databases[i] == null) continue;
			for (Sds key : databases[i].getDict().keySet()) {
				JedisObject value = databases[i].get(key);
				String insertCmd = value.insertCommand(key);
				aFile.writeBytes(insertCmd);
				aFile.writeBytes("\r\n");
			}
		}
		aFile.close();
		File oldFile = new File(fileName);
		File newFile = new File(aofPathName);
		if(newFile.exists()) newFile.delete();
		oldFile.renameTo(newFile);
		return true;
	}
	
	public static void load(String aofPathName) throws IOException{
		RandomAccessFile file = new RandomAccessFile(aofPathName, "r");
		JedisClient fakeClient = new JedisClient("0.0.0.0:0");
		long length = file.length();
		while(length > 0){
			int dbIndex = file.readInt();
			String cmd = file.readLine();
			fakeClient.setCurrentDB(dbIndex);
			CommandLine cl = new CommandLine();
			cl.parse(cmd);
			CommandHandler handler = JedisConfigration.getHandler(cl.getCommand());
			handler.execute(fakeClient, cl);
			length -= file.getFilePointer();
		}
		file.close();
	}
}
