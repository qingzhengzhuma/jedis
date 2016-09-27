package jedis.util;

public class SetHandler implements CommandHandler{
	class Entry{
		private String key;
		private JedisObject value;
		public Entry(String key,JedisObject value) {
			// TODO Auto-generated constructor stub
			this.key = key;
			this.value = value;
		}
	}
	private Entry parseKeyAndValue(String command){
		String[] segs = command.split("\\s");
		return new Entry(segs[1], new Sds(segs[2]));
	}
	
	@Override
	public JedisObject execute(JedisDB[] database, JedisClient client, String command) {
		// TODO Auto-generated method stub
		JedisDB db = database[client.currentDB];
		Entry entry = parseKeyAndValue(command);
		db.set(entry.key, entry.value);
		return new Sds("OK");
	}
}
