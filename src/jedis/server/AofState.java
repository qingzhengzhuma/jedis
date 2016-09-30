package jedis.server;

public enum AofState {
	AOF_ON,
	AOF_OFF,
	WAIT_REWRITE,
}
