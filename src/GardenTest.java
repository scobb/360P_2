import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
				System.out.println("Acquiring order semaphore.");
				orderSem.acquire();
				myOrder = order++;
				System.out.println("Releasing order semaphore.");
				System.out.println("myOrder: " + order);
				orderSem.release();
				TimeUnit.SECONDS.sleep(1);
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
			System.out.println("Trying to finish digging.");
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
				TimeUnit.SECONDS.sleep(2);
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
				TimeUnit.SECONDS.sleep(2);
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
			g.doneFilling();

		}

	}

	@Test
	public void SimpleTest() {
		ExecutorService threadpool = Executors.newCachedThreadPool();
		Garden g = new Garden(5);
		TestMary mary = new TestMary(g);
		TestBenjamin ben = new TestBenjamin(g);
		TestNewton newt = new TestNewton(g);
		Future<?> f1 = threadpool.submit(mary);
		Future<?> f2 = threadpool.submit(ben);
		Future<?> f3 = threadpool.submit(newt);
		try {
		TimeUnit.SECONDS.sleep(5);
		} catch (Exception exc){
			System.out.println("Exception: " + exc.getMessage());
		}
		assertEquals(0, newt.getOrder());
		assertEquals(1, ben.getOrder());
		assertEquals(2, mary.getOrder());

	}

}
