package net.redis.distributed.lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;


public class LockTest {
	static final String NODES = "127.0.0.1:6379, 127.0.0.1:6389, 127.0.0.1:6399";
	
	@Test
	public void test1(){
		LockManager lockManager = new LockManager(NODES);
		RLock lock = lockManager.lock("foo", 5, TimeUnit.SECONDS);
		if(lock==null){
			System.out.println(Thread.currentThread().getName() + "Error, lock not acquired");
		}else{
			System.out.println(Thread.currentThread().getName() + " acquired lock: " + lock);
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			lockManager.unlock(lock);
		}
	}
	
	private static final int DEFAULT_THREAD_COUNT = 10;
	private static final int DEFAULT_LOOP_COUNT = 100;
	
	
	@Test
	public void test2(){
		ExecutorService service = Executors.newFixedThreadPool(DEFAULT_THREAD_COUNT);
		CountDownLatch startSignal = new CountDownLatch(1);
		CountDownLatch doneSignal = new CountDownLatch(DEFAULT_LOOP_COUNT);
		long start = System.currentTimeMillis();
		
		for (int i = 0; i < DEFAULT_LOOP_COUNT; i++) {
			service.execute(new LockTask(startSignal, doneSignal));
		}
		
		try {
			startSignal.countDown();      
			doneSignal.await();           
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("全部执行完毕,耗时："+ (System.currentTimeMillis()-start) + "毫秒");
		service.shutdown();
	}
	
	@Test
	public void test3(){
		for (int i = 0; i < 5; i++) {
			test2();
		}
	}
	
	public class LockTask implements Runnable {
		private final CountDownLatch startSignal;
		private final CountDownLatch doneSignal;
		
		public LockTask(CountDownLatch startSignal, CountDownLatch doneSignal) {
			this.startSignal = startSignal;
			this.doneSignal = doneSignal;
		}
		
		@Override
		public void run() {
			try {
				startSignal.await();
				test1();
				doneSignal.countDown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
