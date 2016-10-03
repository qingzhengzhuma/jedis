package jedis.util;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Sds implements JedisObject{
	private static JedisObject TYPE = new Sds("STRING");
	private static final byte typeCode = '0'; 
	private int used;
	private int free;
	private byte[] content;
	public Sds() {
		// TODO Auto-generated constructor stub
		this.used = 0;
		this.free = 0;
		content = new byte[0];
	}
	
	public Sds(int initLength){
		this.used = 0;
		this.free = initLength;
		this.content = new byte[initLength];
	}
	
	public Sds(byte[] init){
		int length = init == null ? 0 : init.length;
		this.used = length;
		this.free = 0;
		this.content = new byte[length];
		int i = 0;
		for(byte b : init){
			this.content[i++] = b;
		}
	}
	
	public Sds(String init){
		int length = init == null ? 0 : init.length();
		this.used = length;
		this.free = 0;
		this.content = new byte[length];
		for(int i = 0; i < length; ++i){
			this.content[i] = (byte)init.charAt(i);
		}
	}
	
	public Sds(Sds init){
		int length = init == null ? 0 : init.used;
		this.used = length;
		this.free = 0;
		this.content = new byte[length];
		for(int i = 0; i < length; ++i){
			this.content[i] = init.content[i];
		}
	}
	
	public Sds clone(){
		return new Sds(this.content);
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this) return true;
		if(! (o instanceof Sds)) return false;
		Sds other = (Sds)o;
		if(other.used != this.used) return false;
		for(int i = 0; i < used;++i){
			if(other.content[i] != this.content[i]) return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int result = 23;
		result = 37*result + used;
		for(int i = 0; i < used;++i){
			result = result*37 + content[i];
		}
		return result;
	}
	
	@Override
	public String toString(){
		return new String(content, 0, used);
	}
	
	private int getLength(){
		return this.free;
	}
	
	public int length() {
		return this.used;
	}
	
	public int resize(int capacity){
		if(capacity > this.used) {
			byte[] newContent = new byte[capacity];
			for(int i = 0; i < this.used;++i){
				newContent[i] = this.content[i];
			}
			this.content = newContent;
			this.free = this.content.length - this.used;
		}
		return this.content.length;
	}
	
	public void copyFrom(byte[] s,int length){
		if(length < 0){
			throw new IllegalArgumentException();
		}
		if(s == null) length = 0;
		if(s.length < length) length = s.length;
		if(this.content.length < length){
			this.content = new byte[length];	
		}
		for(int i = 0; i < length;++i){
			content[i] = s[i];
		}
		this.used = length;
		this.free = this.content.length - this.used;
	}
	
	public void copyFrom(byte[] s) {
		copyFrom(s,s == null ? 0 : s.length);
	}
	
	public void copyFrom(Sds s,int length){
		copyFrom(s.content, length);
	}
	
	public void copyFrom(Sds s) {
		copyFrom(s.content,s == null ? 0 : s.getLength());
	}
	
	public void append(byte[] s,int length) {
		if(s == null || s.length == 0) length = 0;
		if(length > s.length) length = s.length;
		if(this.free >= length){
			int i = this.used;
			for(int j = 0; j < length;++j){
				this.content[i++] = s[j];
			}
			this.free -= length;
		}else{
			byte[] newContent = new byte[this.used + s.length];
			int i = 0;
			for(; i < this.used;++i){
				newContent[i] = this.content[i];
			}
			for(int j = 0; j < length; ++j){
				newContent[i++] = s[j];
			}
			this.content = newContent;
			this.free = 0;
		}
		this.used += length;
	}
	
	public void append(String s,int length) {
		if(s == null || s.length() == 0) length = 0;
		if(length > s.length()) length = s.length();
		if(this.free >= length){
			int i = this.used;
			for(int j = 0; j < length;++j){
				this.content[i++] = (byte)s.charAt(j);
			}
			this.free -= length;
		}else{
			byte[] newContent = new byte[this.used + length];
			int i = 0;
			for(; i < this.used;++i){
				newContent[i] = this.content[i];
			}
			for(int j = 0; j < length; ++j){
				newContent[i++] = (byte)s.charAt(j);
			}
			this.content = newContent;
			this.free = 0;
		}
		this.used += length;
	}
	
	public void append(String s) {
		append(s,s.length());
	}
	
	public void append(Sds s,int length) {
		append(s.content, length);
	}
	
	public void append(byte[] s) {
		append(s, s == null ? 0 : s.length);
	}
	
	public void append(Sds s) {
		append(s,s == null ? 0 :s.getLength());
	}
	
	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		return toString().getBytes();
	}
	
	@SuppressWarnings("static-access")
	@Override 
	public JedisObject type(){
		return this.TYPE;
	}

	@Override
	public void writeObject(RandomAccessFile file) throws IOException{
		// TODO Auto-generated method stub
		file.writeByte(typeCode);
		file.writeInt(used);
		file.write(content,0,used);
	}
	
	@Override
	public Sds deepCopy(){
		return new Sds(this);
	}
	
	@Override
	public String insertCommand(Sds key){
		Sds sds = new Sds(key.used + used + 5);
		sds.append("set ");
		sds.append(key);
		sds.append(" ");
		sds.append(this);
		return sds.toString();
	}

}
