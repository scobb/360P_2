import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class GardenTest {
	static int order = 0;
	public static Semaphore orderSem = new Semaphore(1);
	public static final int INVALID = -1;

	public static void resetOrder() {
		order = 0;
	}

	public static class TestNewton implements Runnable {
		Garden g;
		int myOrder;

		public TestNewton(Garden g) {
			this.g = g;
			this.myOrder = INVALID;
		}

		public int getOrder() {
			return myOrder;
		}

		@Override
		public void run() {
			try {
				g.startDigging();
				//System.out.println("Acquiring order semaphore.");
				orderSem.acquire();
				myOrder = order++;
				//System.out.println("Releasing order semaphore.");
				//System.out.println("myOrder: " + order);
				orderSem.release();
				// TimeUnit.SECONDS.sleep(1);
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
			// System.out.println("Trying to finish digging.");
			g.doneDigging();

		}

	}

	public static class TestBenjamin implements Runnable {
		Garden g;
		int myOrder;

		public TestBenjamin(Garden g) {
			this.g = g;
			this.myOrder = INVALID;
		}

		public int getOrder() {
			return myOrder;
		}

		@Override
		public void run() {
			try {
				g.startSeeding();
				orderSem.acquire();
				myOrder = order++;
				orderSem.release();
				// TimeUnit.SECONDS.sleep(1);
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
			g.doneSeeding();

		}

	}

	public static class TestMary implements Runnable {
		Garden g;
		int myOrder;

		public TestMary(Garden g) {
			this.g = g;
			this.myOrder = INVALID;
		}

		public int getOrder() {
			return myOrder;
		}

		@Override
		public void run() {
			try {
				g.startFilling();
				orderSem.acquire();
				myOrder = order++;
				orderSem.release();
				// TimeUnit.SECONDS.sleep(1);
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
			g.doneFilling();

		}

	}

	// @Test
	public void SimpleTest() throws InterruptedException, ExecutionException{
		resetOrder();
		ExecutorService threadpool = Executors.newCachedThreadPool();
		Garden g = new Garden(5);
		TestMary mary = new TestMary(g);
		TestBenjamin ben = new TestBenjamin(g);
		TestNewton newt = new TestNewton(g);
		
		// ensure we submit out of order
		Future<?> f1 = threadpool.submit(mary);
		TimeUnit.SECONDS.sleep(1);
		threadpool.submit(ben);
		TimeUnit.SECONDS.sleep(1);
		threadpool.submit(newt);

		// only have to get f1, because this should imply that others are done.
		f1.get();
		assertEquals(0, newt.getOrder());
		assertEquals(1, ben.getOrder());
		assertEquals(2, mary.getOrder());
		assertEquals(1, g.totalHolesDugByNewton());
		assertEquals(1, g.totalHolesSeededByBenjamin());
		assertEquals(1, g.totalHolesFilledByMary());
		
		threadpool.shutdown();
	}
	
	// @Test
	public void tooManyHolesTest() throws InterruptedException, ExecutionException{
		resetOrder();
		ExecutorService threadpool = Executors.newCachedThreadPool();
		Garden g = new Garden(1);
		TestNewton n = new TestNewton(g);
		Future<?> f1 = threadpool.submit(n);
		f1.get();
		assertEquals(0, n.getOrder());
		
		// should not be able to dig until we fill one
		f1 = threadpool.submit(n);
		
		// okay, seed and fill that hole
		threadpool.submit(new TestBenjamin(g));
		assertEquals(1, g.totalHolesDugByNewton());
		threadpool.submit(new TestMary(g));
		
		// should be able to get f1 now, and it should happen last.
		f1.get();
		
		assertEquals(3, n.getOrder());
		threadpool.shutdown();
		assertEquals(2, g.totalHolesDugByNewton());
		assertEquals(1, g.totalHolesSeededByBenjamin());
		assertEquals(1, g.totalHolesFilledByMary());
	}
	
	@Test
	public void bigNumberTest() throws ExecutionException, InterruptedException{
		resetOrder();
		ExecutorService threadpool = Executors.newCachedThreadPool();
		Garden g = new Garden(10);
		for (int i = 0; i < 10; ++i){
			threadpool.submit(new TestNewton(g));
		}
//		System.out.println("newton: " + g.totalHolesDugByNewton());
		assertEquals(0, g.totalHolesFilledByMary());
		assertEquals(0, g.totalHolesSeededByBenjamin());
//		Thread.sleep(3000);
		while (g.totalHolesDugByNewton() != 10){
			Thread.sleep(1000);
		}
		assertEquals(10, g.totalHolesDugByNewton());
		for (int i = 0; i < 10; ++i){
			threadpool.submit(new TestBenjamin(g));
		}
		assertEquals(0, g.totalHolesFilledByMary());
//		System.out.println("newton: " + g.totalHolesDugByNewton());
//		System.out.println("ben: " + g.totalHolesSeededByBenjamin());
//		Thread.sleep(2000);
		while (g.totalHolesSeededByBenjamin() != 10){
			Thread.sleep(1000);
		}
		assertEquals(10, g.totalHolesSeededByBenjamin());
		assertEquals(10, g.totalHolesDugByNewton());
		
		for (int i = 0; i < 10; ++i){
			threadpool.submit(new TestMary(g));

		}
		
		while (g.totalHolesFilledByMary() != 10){
			Thread.sleep(1000);
		}
		threadpool.shutdown();
		assertEquals(10, g.totalHolesSeededByBenjamin());
		assertEquals(10, g.totalHolesDugByNewton());
		assertEquals(10, g.totalHolesFilledByMary());
		
	}
	
	
	
}
