package jedis.util;

public class SaveHandler implements CommandHandler {

	@Override
	public JedisObject execute(JedisDB[] databases, JedisClient client, CommandLine cl)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		RdbSaveThread thread = new RdbSaveThread(databases);
		thread.start();
		return MessageConstant.OK;
	}

}
