import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;


public class FairReadWriteLockTest {
	static int order = 0;
	public void resetOrder(){
		order = 0;
	}
	public static class TestWriter implements Callable<Integer>{
		FairReadWriteLock lock;
		int duration;
		
		public TestWriter(FairReadWriteLock lock, int duration){
			this.lock = lock;
			this.duration = duration;
		}
		@Override
		public Integer call() throws Exception {
			lock.beginWrite();
			try {
				TimeUnit.SECONDS.sleep(duration);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lock.endWrite();
			return order++;
		}
	}
	public static class TestReader implements Callable<Integer>{
		FairReadWriteLock lock;
		int duration;
		
		public TestReader(FairReadWriteLock lock, int duration){
			this.lock = lock;
			this.duration = duration;
		}
		@Override
		public Integer call() throws Exception {
			lock.beginRead();
			try {
				TimeUnit.SECONDS.sleep(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			lock.endRead();
			return order++;
		}
	}
	@Test
	public void singleReaderTest() throws InterruptedException, ExecutionException{
		resetOrder();
		FairReadWriteLock lock = new FairReadWriteLock();
		ExecutorService threadpool = Executors.newCachedThreadPool();
		Future<Integer> result = threadpool.submit(new TestReader(lock, 1));
		assertEquals(0, (int)result.get());
		threadpool.shutdown();
	}
}
