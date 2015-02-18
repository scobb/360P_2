
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
	
	public FairReadWriteLock(){
		// implement a "take a number" methodology?
		schedule = 0;
		turn = 0;
		
		numWriters = 0;
		numReaders = 0;
		
	}
	
	void beginRead(){
		// Take a number.
		int myScheduledTurn;
		synchronized(this){
			myScheduledTurn = schedule;
			schedule++;
		}
		
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
		synchronized(this){
			turn++;
			
			// because multiple people can read, we need to notify here to let other readers in.
			notifyAll();
		}
		
	}
	synchronized void endRead(){
		numReaders--;
		notifyAll();
		
	}
	void beginWrite(){
		
		// take a number
		int myScheduledTurn;

		synchronized(this){
			myScheduledTurn = schedule;
			schedule++;
		}
		
		// if there's a reader, a writer, or it's not my turn, wait.
		while (numReaders > 0 || numWriters > 0 || myScheduledTurn < turn){
			try {
				wait();
			} catch (InterruptedException exc){
				System.out.println("Exception: " + exc);
			}
		}
		
		// once we get in, increment the number of writers
		numWriters++;
		
		// increment the turn.
		synchronized(this){
			turn++;
		}
		
	}
	synchronized void endWrite(){
		numWriters--;
		notifyAll();
		
	}
}
