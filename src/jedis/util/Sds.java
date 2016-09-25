package jedis.util;

public class Sds{
	private int used;
	private int free;
	private char[] content;
	public Sds() {
		// TODO Auto-generated constructor stub
		this.used = 0;
		this.free = 0;
		content = new char[0];
	}
	
	public Sds(int initLength){
		this.used = 0;
		this.free = initLength;
		this.content = new char[initLength];
	}
	
	public Sds(char[] init){
		int length = init == null ? 0 : init.length;
		this.used = length;
		this.free = 0;
		this.content = new char[length];
		int i = 0;
		for(char c : init){
			this.content[i++] = c;
		}
	}
	
	public Sds(String init){
		int length = init == null ? 0 : init.length();
		this.used = length;
		this.free = 0;
		this.content = new char[length];
		for(int i = 0; i < length; ++i){
			this.content[i] = init.charAt(i);
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
			char[] newContent = new char[capacity];
			for(int i = 0; i < this.used;++i){
				newContent[i] = this.content[i];
			}
			this.content = newContent;
			this.free = this.content.length - this.used;
		}
		return this.content.length;
	}
	
	public void copyFrom(char[] s,int length){
		if(length < 0){
			throw new IllegalArgumentException();
		}
		if(s == null) length = 0;
		if(s.length < length) length = s.length;
		if(this.content.length < length){
			this.content = new char[length];	
		}
		for(int i = 0; i < length;++i){
			content[i] = s[i];
		}
		this.used = length;
		this.free = this.content.length - this.used;
	}
	
	public void copyFrom(char[] s) {
		copyFrom(s,s == null ? 0 : s.length);
	}
	
	public void copyFrom(Sds s,int length){
		copyFrom(s.content, length);
	}
	
	public void copyFrom(Sds s) {
		copyFrom(s.content,s == null ? 0 : s.getLength());
	}
	
	public void append(char[] s,int length) {
		if(s == null || s.length == 0) length = 0;
		if(length > s.length) length = s.length;
		if(this.free >= length){
			int i = this.used;
			for(int j = 0; j < length;++j){
				this.content[i++] = s[j];
			}
			this.free -= length;
		}else{
			char[] newContent = new char[this.used + s.length];
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
	
	public void append(Sds s,int length) {
		append(s.content, length);
	}
	
	public void append(char[] s) {
		append(s, s == null ? 0 : s.length);
	}
	
	public void append(Sds s) {
		append(s,s == null ? 0 :s.getLength());
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
