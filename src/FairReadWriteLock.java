import java.util.concurrent.Semaphore;


public class FairReadWriteLock {
	// REQUIREMENTS:
	// (0) Use monitor -- wait, notify, notifyAll
	// (a) no rw or ww conflict
	// (b) writer thread that invokes beginWrite() will be blocked until all preceding
	//     reader and writer threads have acquired and released the lock
	// (c) a reader thread that invokes beginRead() will be blocked until all preceding writer
	//     threads have acquired and released the lock
	// (d) a reader thread cannot be blocked if all preceding writer threads have acquired and
	//     released the lock or no preceding writer thread exists
	
	// TODO - write tests.
	private int schedule;
	private int turn;
	
	private int numWriters;
	private int numReaders;
	
	private Semaphore sem;
	
	public FairReadWriteLock(){
		// implement a "take a number" methodology?
		schedule = 0;
		turn = 0;
		
		numWriters = 0;
		numReaders = 0;
		
		// are we allowed to use a semaphore?
		sem = new Semaphore(1);
		
	}
	private int getScheduledTurn(){
		// helper method - acquires the semaphore to alter the shared variable schedule
		try {
			sem.acquire();
		} catch (InterruptedException exc){
			System.out.println("Exception: " + exc);
		}
		int result = schedule;
		schedule++;
		sem.release();
		return result;
	}
	
	private void incrementTurn(){
		// helper method - acquires the semaphore to alter the shared variable turn
		try {
			sem.acquire();
		} catch (InterruptedException exc){
			System.out.println("Exception: " + exc);
		}
		turn++;
		sem.release();
	}
	
	void beginRead(){
		// Take a number.
		int myScheduledTurn = getScheduledTurn();
		
		// ensure there are no writers and it's my turn to read
		while (numWriters > 0 || myScheduledTurn < turn){
			try {
				wait();
			} catch (InterruptedException exc){
				System.out.println("Exception: " + exc);
			}
		}
		
		// once we get in, increment number of readers
		numReaders++;
		
		// when I'm in, it's the next guy's turn.
		incrementTurn();
		
		// because multiple people can read, we need to notify here to let other readers in.
		notifyAll();
	}
	void endRead(){
		numReaders--;
		notifyAll();
		
	}
	void beginWrite(){
		
		// take a number
		int myScheduledTurn = getScheduledTurn();
		
		// if there's a reader, a writer, or it's not my turn, wait.
		while (numReaders > 0 || numWriters > 0 || myScheduledTurn < turn){
			try {
				wait();
			} catch (InterruptedException exc){
				System.out.println("Exception: " + exc);
			}
		}
		
		// increment the turn.
		incrementTurn();
		
	}
	void endWrite(){
		numWriters--;
		notifyAll();
		
	}
}
