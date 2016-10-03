package jedis.server;

public enum MultiState {
	NONE,
	WAIT_EXEC,
	ERROR_HAPPENED,
}
