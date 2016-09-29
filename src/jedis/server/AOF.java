package jedis.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import jedis.util.CommandLine;

public class AOF {
	RandomAccessFile aofFile;
	private AOF(RandomAccessFile aofFile) {
		this.aofFile = aofFile;
	}
	private static AOF open(String filePathName,String mode) throws FileNotFoundException{
		RandomAccessFile file = new RandomAccessFile(filePathName,mode);
		return new AOF(file);
	}
	public static AOF openForRead(String filePathName) throws FileNotFoundException{
		return open(filePathName, "r");
	}
	
	public static AOF openForAppend(String filePathName) throws FileNotFoundException{
		return open(filePathName, "rw");
	}
	
	public void put(CommandLine cmd) throws IOException{
		aofFile.writeBytes(cmd.getNormalizedCmdLine());
		aofFile.writeBytes("\r\n");
	}
	
	public String next() throws IOException{
		return aofFile.readLine();
	}
}
