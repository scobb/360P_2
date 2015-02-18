import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class Garden {
	// implement the following function
	// don't worry about exceptions
	final private ReentrantLock lock = new ReentrantLock();
	final private Condition readyToSeed = lock.newCondition();
	final private Condition readyToFill = lock.newCondition();
	final private Condition readyToDig = lock.newCondition();
	
	final private ReentrantLock shovel = new ReentrantLock();
	
	private int numUnseededHoles = 0;
	private int numSeededHoles = 0;
	private int max_holes;
	private boolean shovelAvailable = true;
	
	
	// the constructor takes the MAX argument
	public Garden(int MAX){
		max_holes = MAX;
	}
	
	public void startDigging() throws InterruptedException{
		try {
			lock.lock();
			System.out.println("Trying to start digging.");
			int totalHoles = numUnseededHoles + numSeededHoles;
			while (totalHoles == max_holes || !shovelAvailable)
				readyToDig.await();
			System.out.println("Starting digging.");
			shovelAvailable = false;
		} finally {
			lock.unlock();
		}
		
		
	}
	public void doneDigging(){
		try {
			System.out.println("Trying to acquire lock.");
			lock.lock();
			System.out.println("Lock acquired.");
			++numUnseededHoles;
			shovelAvailable = true;
			
			
			// signal for hole
			System.out.println("signaling readyToSeed.");
			readyToSeed.signal();
			// signal for shovel
			System.out.println("signaling readyToFill.");
			readyToFill.signal();
			System.out.println("Done digging.");
		} finally {
			lock.unlock();
		}
		
	}
	public void startSeeding() throws InterruptedException{
		try {
			System.out.println("Trying to start seeding.");
			lock.lock();
			while (numUnseededHoles == 0) { 
				readyToSeed.await();
				System.out.println("Just got signalled in readyToSeed.");
			}
			System.out.println("Starting seeding.");
		} finally {
			lock.unlock();
		}
	}
	public void doneSeeding(){
		try {
			lock.lock();
			++numSeededHoles;
			--numUnseededHoles;
			readyToFill.signal();
			System.out.println("Done seeding.");
		} finally {
			lock.unlock();
		}
		
	}
	public void startFilling() throws InterruptedException{
		try {
			lock.lock();
			System.out.println("Trying to start filling.");
			while (numSeededHoles == 0 || ! shovelAvailable)
				readyToFill.await();
			shovelAvailable = false;
			System.out.println("Filling started.");
		} finally {
			lock.unlock();
		}
		
	}
	public void doneFilling(){
		try {
			lock.lock();
			--numSeededHoles;
			shovelAvailable = true;
			readyToDig.signal();
			System.out.println("Filling done.");
		} finally {
			lock.unlock();
		}
		
		
	}

}
