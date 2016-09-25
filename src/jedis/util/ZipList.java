package jedis.util;

import java.util.Iterator;

public class ZipList{
	private int entrySize;
	private int blobSize;
	private final static int ENTRY_LENGTH_THRESHOLD = 255;
	/**
	 * Container for the ziplist content. In the container, an entry is
	 * consist of two part: the length of the entry and the entry's value.
	 * if the length of an entry is greater than ENTRY_LENGTH_THRESHHOLD,
	 * we will use 5 bytes to store the entry's length which the first byte
	 * is indicated by ENTRY_LENGTH_THRESHHOLD and the followed 4 bytes will 
	 * store the real length of the entry's value. Otherwise, we will only
	 * use 1 byte to store the length, then the value followed.
	 */
	private char[] content; 

	public ZipList(){
		this.entrySize = 0;
		this.blobSize = 0;
		this.content = null;
	}
	
	private void checkIndex(int index){
		if(index < 0 || index >= this.entrySize){
			throw new IndexOutOfBoundsException();
		}
	}
	
	/**
	 * Parse the content length of an entry
	 * @param start The start index of the entry in the Container
	 * @param content Container
	 * @return
	 */
	private int getEntryLength(int start,char[] content){
		int entryLength = content[start++];
		if(entryLength == ENTRY_LENGTH_THRESHOLD){
			entryLength = Integer.MAX_VALUE & content[start++]<<24;
			entryLength &= content[start++]<<16;
			entryLength &= content[start++]<<8;
			entryLength &= content[start++];
		}
		return entryLength;
	}
	
	/**
	 * Write an entry's length to the beginning of the entry 
	 * @param start Start index of the entry in the container
	 * @param content container of ziplist
	 * @param entryLength length of the entry to be written
	 * @return 1 or 5,bytes used to store the length 
	 */
	private int setEntryLength(int start,char[] content,int entryLength){
		int bytes = 1;
		if(entryLength < ENTRY_LENGTH_THRESHOLD) content[start++] = (char) entryLength;
		else {
			content[start++] = (char)255;
			content[start++] = (char)(entryLength & (255<<24));
			content[start++] = (char)(entryLength & (255<<16));
			content[start++] = (char)(entryLength & (255<<8));
			content[start++] = (char)(entryLength & (255));
			bytes+=4;
		}
		return bytes;
	}
	
	/**
	 * Write an entry to specified position.
	 * @param start
	 * @param content
	 * @param entry
	 * @return bytes used to store the entry, including the length and the value
	 */
	private int setEntry(int start,char[] content,char[] entry){
		start += setEntryLength(start, content, entry.length);
		for(int i = 0; i < entry.length;++i){
			content[start++] = entry[i];
		}
		return entry.length + (entry.length >= ENTRY_LENGTH_THRESHOLD ? 5 : 1);
	}
	
	/**
	 * Find the start position of ith entry in the container
	 * @param index
	 * @return start position of the ith entry int the container
	 */
	private int findStartIndexOf(int index){
		checkIndex(index);
		int start = 0;
		for(int i = 0; i < index;++i){
			int entryLength = getEntryLength(start, this.content);
			start += entryLength + 1;
			if(entryLength > 254) start += 4;
		}
		return start;
	}
	
	@Override
	public String toString(){
		return new String(this.content);
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this) return true;
		if(!(o instanceof ZipList)) return false;
		ZipList other = (ZipList)o;
		if(other.blobSize != this.blobSize || other.entrySize != this.entrySize) return false;
		for(int i = 0; i < this.blobSize;++i){
			if(other.content[i] != this.content[i]) return false;
		}
		return true;
	}
	
	@Override
	public int hashCode(){
		int result = 23;
		result = 37*result + this.entrySize;
		result = 37*result + this.blobSize;
		for(char c : this.content){
			result = 37*result + c;
		}
		return result;
	}
	
	/**
	 * Get the count of entries currently stored in the container 
	 * @return
	 */
	public int getEntrySize(){
		return this.entrySize;
	}
	
	/**
	 * Get the length of the container in bytes, it must be greater
	 * than the entry count
	 * @return
	 */
	public int getBlobSize(){
		return this.blobSize;
	}
	
	/**
	 * Insert an entry into the list after or before the specified index.
	 * @param entry the new entry to be inserted
	 * @param index Specified postion to insert an ne entry
	 * @param after if true, the new entry will be inserted after the 
	 * specified postion,else will be insert before the specified postion
	 * @throws IndexOutOfBoundsException if the specified position is
	 * beyond the current range of the Ziplist
	 */
	public void insert(char[] entry, int index,boolean after){
		if(entry == null || entry.length == 0) return;
		checkIndex(index);
		int newBlobSize = this.blobSize + entry.length + 1;
		if(entry.length > 254){
			newBlobSize += 4;
		}
		char[] newContent = new char[newBlobSize];
		int i = 0,j = 0;
		int count = 0;
		while(count < index + (after ? 1 : 0)){
			int entryLength = getEntryLength(i++,this.content);
			if(entryLength > 254){
				i += 4;
			}
			j += setEntryLength(j, newContent, entryLength);
			for(int k = 0;k < entryLength;++k){
				newContent[j++] = this.content[i++];
			}
			++count;
		}
		j += setEntry(j, newContent, entry);
		while(i < this.blobSize){
			newContent[j++] = this.content[i++];
		}
		this.content = newContent;
		this.entrySize += 1;
		this.blobSize = newBlobSize;
	}
	
	/**
	 * push a new entry to the end of the list
	 * @param entry
	 */
	public void push(char[] entry){
		if(entry == null || entry.length == 0) return;
		if(this.entrySize == 0){
			int tempBlobSize = entry.length + 1;
			if(entry.length >= ENTRY_LENGTH_THRESHOLD){
				tempBlobSize += 4;
			}
			this.content = new char[tempBlobSize];
			setEntry(0, this.content, entry);
			this.blobSize = tempBlobSize;
			this.entrySize = 1;
		}else{
			insert(entry, this.entrySize - 1, true);
		}
	}
	
	/**
	 * Remove the entry at the specified position
	 * @param index Specify the position of the entry to be removed
	 * @throws IndexOutOfBoundsException if the specified position is
	 * beyond the current range of the Ziplist
	 */
	public void remove(int index){
		checkIndex(index);
		int j = 0;
		for(int i = 0; i < index;++i){
			int entryLength = getEntryLength(j, this.content);
			j += entryLength + 1;
			if(entryLength > 254) j += 4;
		}
		int entryLength = getEntryLength(j, this.content);
		int k = j + entryLength + 1;
		if(entryLength > 254) k += 4;
		while(k < this.blobSize){
			this.content[j++] = this.content[k++];
		}
		this.entrySize -= 1;
		this.blobSize -= entryLength + 1;
		if(entryLength > 254) this.blobSize -= 4;
		char[] newContent = new char[this.blobSize];
		j = 0;
		while(j < this.blobSize){
			newContent[j] = this.content[j++];
		}
		this.content = newContent;
	}
	
	/**
	 * remove COUNT entries in sequence from the the container,
	 * the first entry to be removed is specified by start.
	 * @param start
	 * @param count
	 */
	public void removeRange(int start,int count){
		if(start < 0 || start >= this.entrySize){
			throw new IndexOutOfBoundsException();
		}
		if(count < 1 || start + count > this.entrySize){
			throw new IllegalArgumentException();
		}
		int index = findStartIndexOf(start);
		int i = 0,j = index;
		while(i < count){
			int length = getEntryLength(j, this.content);
			j += length + (length >= ENTRY_LENGTH_THRESHOLD ? 5 : 1);
			++i;
		}
		int newBlobSize = this.blobSize - (j - index);
		char[] newContent = new char[newBlobSize];
		i = 0;
		for(int k = 0; k < index;++k){
			newContent[i++] = this.content[k];
		}
		for(int k = j; k < this.blobSize;++k){
			newContent[i++] = this.content[k];
		}
		this.entrySize -= count;
		this.blobSize = newBlobSize;
		this.content = newContent;
	}
	
	/**
	 * Get the entry at the specified position
	 * @param index Specify the position of the entry to be removed
	 * @throws IndexOutOfBoundsException if the specified position is
	 * beyond the current range of the Ziplist
	 */
	public char[] get(int index){
		checkIndex(index);
		int start = findStartIndexOf(index);
		int entryLenth = getEntryLength(start, this.content);
		start += 1;
		if(entryLenth > 254) start += 4;
		char[] entryContent = new char[entryLenth];
		for(int i = 0; i < entryLenth;++i){
			entryContent[i] = this.content[start++];
		}
		return entryContent;
	}
	
	/**
	 * Merge two ziplist, the function will create a new ziplist,
	 * it will not change the input ziplists 
	 * @param other
	 * @return
	 */
	public ZipList merge(ZipList other) {
		ZipList result = new ZipList();
		int resultBlocSize = this.blobSize + (other == null ? 0 : other.blobSize);
		result.content = new char[resultBlocSize];
		int i = 0;
		for(char c : this.content){
			result.content[i++] = c;
		}
		if(other != null){
			for(char c : other.content){
				result.content[i++] = c;
			}
		}
		result.entrySize = this.entrySize + (other == null ? 0 : other.entrySize);
		result.blobSize = this.blobSize + (other == null ? 0 : other.blobSize);
		return result;
	}
	
	/**
	 * traverse the ziplist from the beginning to the end to find the first entry
	 * in the container equals to the given entry
	 * @param entry
	 * @return the index of the first entry equals to the given entry, if not found,
	 * -1 will be returned
	 */
	public int find(char[] entry){
		Iterator<char[]> iterator = this.iterator();
		int j = 0;
		while(iterator.hasNext()){
			char[] e = iterator.next();
			if(e.length == entry.length){
				int i = 0;
				for(; i < e.length;++i){
					if(e[i] != entry[i]) break;
				}
				if( i == e.length) return j;
			}
			++j;
		}
		return -1;
	}
	
	/**
	 * Get a forward iterator of the ziplist
	 * @return
	 */
	public Iterator<char[]> iterator(){
		return new Iterator<char[]>() {
			int i = 0;
			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return i < blobSize;
			}

			@Override
			public char[] next() {
				// TODO Auto-generated method stub
				int entryLength = getEntryLength(i, content);
				i += 1;
				if(entryLength > 254) i += 4;
				char[] entryContent = new char[entryLength];
				for(int j = 0; j < entryLength;++j){
					entryContent[j] = content[i++];
				}
				return entryContent;
			}
		};
	}
	
	/**
	 * Get a backward iterator of the ziplist
	 * @return
	 */
	public Iterator<char[]> reverseIterator(){
		return new Iterator<char[]>() {
			int index = entrySize;
			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return index > 0;
			}

			@Override
			public char[] next() {
				// TODO Auto-generated method stub
				return get(--index);
			}
		};
	}
	
	public static void main(String[] args) {
		ZipList zipList = new ZipList();
		char[] entry = "sgedhtgrherjrtyjtyukerthwerutykyujtgehrwujuytrjtyjherethrhrehello".toCharArray();
		zipList.push(entry);
		System.out.println(zipList.getBlobSize());
		System.out.println(zipList.getEntrySize());
	}
}
