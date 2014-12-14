package net.redis.distributed.lock;

import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.substringBefore;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.redis.distributed.lock.util.JedisUtils;
import redis.clients.jedis.Jedis;

public class LockManager implements ILock{
	
	private List<Jedis> nodeList = new ArrayList<Jedis>();
	private int retryCount = 3;
	private long retryDelay = 200;
	private double clockDriftFactor = 0.01;
	private int quorum;
	
	static final String unlockScript = 
			"if redis.call(\"get\",KEYS[1]) == ARGV[1] then " +
			"	return redis.call(\"del\",KEYS[1]) " + 
			"else " +
			"	return 0 " +
			"end ";
	
	static final String COLON = ":";
	static final String COMMA = ",";
	
	public LockManager(String nodes){
		buildNode(nodes, JedisUtils.DEFAULT_TIMEOUT);
	}
	public LockManager(String nodes, int timeout, TimeUnit unit){
		buildNode(nodes, (int)unit.toMillis(timeout));
	}
	protected void buildNode(String nodes, int timeout){
		for (String clusterNode : split(nodes, COMMA)) {
			String host = substringBefore(clusterNode, COLON);
			String port = substringAfter(clusterNode, COLON);
			Jedis jedis = new Jedis(host, Integer.parseInt(port), timeout);
			nodeList.add(jedis);
		}
		quorum = nodeList.size()/2 + 1;
	}
	
	@Override
	public RLock lock(String resource, long ttl, TimeUnit unit) {
		String value = getUniqueLockValue();
		int retry = retryCount;
		do{
			int num = 0;
			long starttime = System.currentTimeMillis();
			for (Jedis jedis : nodeList) {
				boolean result = lockNode(jedis, resource, value, unit.toMillis(ttl));
				if(result){
					num++;
				}
			}
			long drift = (long)((ttl*clockDriftFactor) + 2);
			long validityTime = ttl - (System.currentTimeMillis() - starttime) - drift;
			
			if(num >= quorum && validityTime > 0){
				return new RLock(validityTime, resource, value);
			}else{
				for (Jedis jedis : nodeList) {
					unlockNode(jedis, resource, value);
				}
			}
			
			long delay = new Random(retryDelay).nextLong();
			try {
				TimeUnit.MILLISECONDS.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			retry--;
		}while(retry > 0);
		
		return null;
	}
	
	private boolean lockNode(Jedis jedis, String resource, String value, long ttl){
		return JedisUtils.setnxex(jedis, resource, value, ttl);
	}
	
	private void unlockNode(Jedis jedis, String resource, String value){
		String[] keys = {resource};
		String[] args = {value};
		JedisUtils.evalScript(jedis, unlockScript, keys, args);
	}
	
	protected String getUniqueLockValue(){
		String value = Thread.currentThread().getId() + "-" + new SecureRandom().nextInt(10000);
		return value;
	}
	

	@Override
	public void unlock(RLock lock) {
		for (Jedis jedis : nodeList) {
			unlockNode(jedis, lock.getResource(), lock.getValue());
		}
	}

	@Override
	public void setRetry(int count, long delay, TimeUnit unit) {
		this.retryCount = count;
		this.retryDelay = unit.toMillis(delay);
	}

}
