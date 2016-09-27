package jedis.util;

import java.util.ArrayList;
import java.util.List;

public class CommandLine {
	private static List<String> segs;
	private static String normalizedCmdLine;
	private static String normalizedCmd;
	public static boolean parse(String cmdLine){
		segs = null;
		normalizedCmdLine = null;
		normalizedCmd = null;
		if(cmdLine == null) return false;
		cmdLine = cmdLine.trim();
		if(cmdLine.length() < 1) return false;
		segs = new ArrayList<>();
		for(int i = 0; i < cmdLine.length();){
			StringBuilder builder = new StringBuilder();
			while(i < cmdLine.length() && 
					!Character.isWhitespace(cmdLine.charAt(i))){
				builder.append(cmdLine.charAt(i++));
			}
			while(i < cmdLine.length() && 
					Character.isWhitespace(cmdLine.charAt(i))){
				++i;
			}
			segs.add(builder.toString());
			
		}
		return true;
	}
	
	public static boolean parse(byte[] cmdLine){
		segs = null;
		normalizedCmdLine = null;
		normalizedCmd = null;
		if(cmdLine == null || cmdLine.length < 1) return false;
		int i = 0,j = cmdLine.length - 1;
		while(i < cmdLine.length && Character.isWhitespace(cmdLine[i])){
			++i;
		}
		while(j >= 0 && Character.isWhitespace(cmdLine[j])){
			--j;
		}
		if(i > j) return false;
		segs = new ArrayList<>();
		while(i <= j){
			StringBuilder builder = new StringBuilder();
			while(i <= j && 
					!Character.isWhitespace(cmdLine[i])){
				builder.append((char)cmdLine[i++]);
			}
			while(i <= j &&
					Character.isWhitespace(cmdLine[i])){
				++i;
			}
			segs.add(builder.toString());
			
		}
		return true;
	}
	public static int getArgc(){
		return segs.size() - 1;
	}
	
	public static String getCommand() {
		return segs.get(0);
	}
	
	public static String getNormalizedCmdLine(){
		if(normalizedCmdLine == null){
			normalizedCmdLine = segs.get(0).toLowerCase();
			for(int i = 1; i < segs.size();++i){
				normalizedCmdLine += " " + segs.get(i);
			}
		}
		return normalizedCmdLine;
	}
	
	public static String getNormalizedCmd(){
		if(normalizedCmd == null){
			normalizedCmd = segs.get(0).toLowerCase();
		}
		return normalizedCmd;
	}
}
