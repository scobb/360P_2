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
			int totalHoles = numUnseededHoles + numSeededHoles;
			while (totalHoles == max_holes || !shovelAvailable)
				readyToDig.await();
			shovelAvailable = false;
		} finally {
			lock.unlock();
		}
		
		
	}
	public void doneDigging(){
		try {
			lock.lock();
			++numUnseededHoles;
			shovelAvailable = true;
			
			// notify for shovel
			readyToFill.notify();
			
			// notify for hole
			readyToSeed.notify();
		} finally {
			lock.unlock();
		}
		
	}
	public void startSeeding() throws InterruptedException{
		try {
			lock.lock();
			while (numUnseededHoles == 0)
				readyToSeed.await();
		} finally {
			lock.unlock();
		}
	}
	public void doneSeeding(){
		try {
			lock.lock();
			++numSeededHoles;
			--numUnseededHoles;
			readyToFill.notify();
		} finally {
			lock.unlock();
		}
		
	}
	public void startFilling() throws InterruptedException{
		try {
			lock.lock();
			while (numSeededHoles == 0 || ! shovelAvailable)
				readyToFill.await();
			shovelAvailable = false;
		} finally {
			lock.unlock();
		}
		
	}
	public void doneFilling(){
		try {
			lock.lock();
			--numSeededHoles;
			shovelAvailable = true;
			readyToDig.notify();
		} finally {
			lock.unlock();
		}
		
		
	}

}
