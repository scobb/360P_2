import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Garden implements GardenCounts {
	// implement the following function
	// don't worry about exceptions
	final private ReentrantLock lock = new ReentrantLock();
	final private Condition readyToSeed = lock.newCondition();
	final private Condition readyToFill = lock.newCondition();
	final private Condition readyToDig = lock.newCondition();

	private int numUnseededHoles = 0;
	private int numSeededHoles = 0;
	private int max_holes;
	private boolean shovelAvailable = true;
	private AtomicInteger totalHolesDug = new AtomicInteger(0);
	private AtomicInteger totalHolesSeeded = new AtomicInteger(0);
	private AtomicInteger totalHolesFilled = new AtomicInteger(0);

	// the constructor takes the MAX argument
	public Garden(int MAX) {
		max_holes = MAX;
	}

	public int totalHolesDugByNewton() {
		return totalHolesDug.get();
	}

	public int totalHolesSeededByBenjamin() {
		return totalHolesSeeded.get();
	}

	public int totalHolesFilledByMary() {
		return totalHolesFilled.get();
	}

	public void startDigging() {
		try {
			lock.lock();
			// System.out.println("Trying to start digging.");
			int totalHoles = numUnseededHoles + numSeededHoles;
			while (totalHoles == max_holes || !shovelAvailable) {
				try {
					readyToDig.await();
				} catch (InterruptedException exc) {
				}
				totalHoles = numUnseededHoles + numSeededHoles;
				// System.out.println("I was signalled.");

			}

			// System.out.println("Starting digging.");
			shovelAvailable = false;
		} finally {
			lock.unlock();
		}

	}

	public void doneDigging() {
		try {
			// System.out.println("Trying to acquire lock.");
			lock.lock();
			// System.out.println("Lock acquired.");
			++numUnseededHoles;
			totalHolesDug.getAndIncrement();
			shovelAvailable = true;

			// signal for hole
			// System.out.println("signaling readyToSeed.");
			readyToSeed.signal();
			// signal for shovel
			// System.out.println("signaling readyToFill.");
			readyToFill.signal();
			// System.out.println("Done digging.");
		} finally {
			lock.unlock();
		}

	}

	public void startSeeding() {
		try {
			// System.out.println("Trying to start seeding.");
			lock.lock();
			while (numUnseededHoles == 0) {
				try {
					readyToSeed.await();
				} catch (InterruptedException exc) {
				}
			}
			// System.out.println("Starting seeding.");
		} finally {
			lock.unlock();
		}
	}

	public void doneSeeding() {
		try {
			lock.lock();
			totalHolesSeeded.getAndIncrement();
			++numSeededHoles;
			--numUnseededHoles;
			readyToFill.signal();
			// System.out.println("Done seeding.");
		} finally {
			lock.unlock();
		}

	}

	public void startFilling() {
		try {
			lock.lock();
			// System.out.println("Trying to start filling.");
			while (numSeededHoles == 0 || !shovelAvailable) {
				try {
					readyToFill.await();
				} catch (InterruptedException exc) {
				}

			}
			shovelAvailable = false;
			// System.out.println("Filling started.");
		} finally {
			lock.unlock();
		}

	}

	public void doneFilling() {
		try {
			lock.lock();
			totalHolesFilled.getAndIncrement();
			--numSeededHoles;
			shovelAvailable = true;
			readyToDig.signal();
			// System.out.println("Filling done.");
		} finally {
			lock.unlock();
		}

	}

}
