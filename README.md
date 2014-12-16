redis-distributed-lock
======================

This Java lib implements the Redis-based distributed lock manager algorithm  http://redis.io/topics/distlock

To create a lock manager:

```
  LockManager lockManager = new LockManager("127.0.0.1:6379, 127.0.0.1:6389, 127.0.0.1:6399");
```
To acquire a lock:

```
  RLock lock = lockManager.lock("foo", 5, TimeUnit.SECONDS);
```
To release a lock:

```
  lockManager.unlock(lock);
```
