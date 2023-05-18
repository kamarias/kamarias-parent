package io.github.kamarias.lock;

/**
 * 分布式锁接口抽象类
 * @author 王玉星
 */
public abstract class AbstractDistributedLock implements DistributedLock {

	private static final long TIMEOUT_MILLIS = 30 * 1000L;

	private static final int RETRY_TIMES = Integer.MAX_VALUE;

	private static final long SLEEP_MILLIS = 500L;

	@Override
	public boolean lock(String key) {
		return lock(key, TIMEOUT_MILLIS, RETRY_TIMES, SLEEP_MILLIS);
	}

	@Override
	public boolean lock(String key, int retryTimes) {
		return lock(key, TIMEOUT_MILLIS, retryTimes, SLEEP_MILLIS);
	}

	@Override
	public boolean lock(String key, int retryTimes, long sleepMillis) {
		return lock(key, TIMEOUT_MILLIS, retryTimes, sleepMillis);
	}

	@Override
	public boolean lock(String key, long expire) {
		return lock(key, expire, RETRY_TIMES, SLEEP_MILLIS);
	}

	@Override
	public boolean lock(String key, long expire, int retryTimes) {
		return lock(key, expire, retryTimes, SLEEP_MILLIS);
	}

}
