
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
	
	private ReadWriteLockLogger logger = new ReadWriteLockLogger();
	
	public FairReadWriteLock(){
		// implement a "take a number" methodology?
		schedule = 0;
		turn = 0;
		
		numWriters = 0;
		numReaders = 0;
		
	}
	
	synchronized void beginRead(){
		// Take a number.
	    logger.logTryToRead(); 
		int myScheduledTurn;
		myScheduledTurn = schedule;
		// System.out.println("beginRead: myScheduledTurn: " + schedule);
		schedule++;
		
		
		// ensure there are no writers and it's my turn to read
		while (numWriters > 0 || turn < myScheduledTurn){
			try {
				wait();
			} catch (InterruptedException exc){
				System.out.println("Exception: " + exc);
			}
		}
		
		// once we get in, increment number of readers
		numReaders++;
		
		// when I'm in, it's the next guy's turn.
		turn++;
		// System.out.println("turn: " + turn);
			
		// because multiple people can read, we need to notify here to let other readers in.
		notifyAll();
	    logger.logBeginRead(); 
	}
	synchronized void endRead(){
		numReaders--;
		// System.out.println("endRead. numReaders: " + numReaders);
		if (numReaders == 0)
			notifyAll();
		logger.logEndRead();
		
	}
	synchronized void beginWrite(){
		logger.logTryToWrite();
		
		// take a number
		int myScheduledTurn;
		myScheduledTurn = schedule;
		// System.out.println("beginWrite: myScheduledTurn: " + schedule);
		schedule++;
		
		// if there's a reader, a writer, or it's not my turn, wait.
		while (numReaders > 0 || numWriters > 0 || turn < myScheduledTurn){
			try {
				// System.out.println("Waiting. numReaders: " + numReaders + "numWriters: " + numWriters + " myScheduledTurn: " + myScheduledTurn + "turn: " + turn);
				wait();
			} catch (InterruptedException exc){
				System.out.println("Exception: " + exc);
			}
		}
		
		// once we get in, increment the number of writers
		numWriters++;
		
		// increment the turn
		turn++;
		
		logger.logBeginWrite();
		
	}
	synchronized void endWrite(){
		numWriters--;
		// System.out.println("endWrite. numWriters: " + numWriters);
		if (numWriters == 0)
			notifyAll();
		logger.logEndWrite();
		
	}
}
