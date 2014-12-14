package net.redis.distributed.lock;

import java.util.concurrent.TimeUnit;

public interface ILock {

	RLock lock(String resource, long ttl, TimeUnit unit);
	
	void unlock(RLock lock);
	
	void setRetry(int count, long delay, TimeUnit unit);
}
