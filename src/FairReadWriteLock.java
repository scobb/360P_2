
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
	// TODO - this is not fair
	private int numReaders;
	private int numWriters;
	public FairReadWriteLock(){
		int numReaders = 0;
		int numWriters = 0;
	}
	void beginRead(){
		while (numWriters > 0){
			try {
				wait();
			} catch (InterruptedException exc){
				System.out.println("Exception: " + exc);
			}
		}
		numReaders++;
		
	}
	void endRead(){
		numReaders--;
		notifyAll();
		
	}
	void beginWrite(){
		while (numReaders > 0 || numWriters > 0){
			try {
				wait();
			} catch (InterruptedException exc){
				System.out.println("Exception: " + exc);
			}
		}
		
	}
	void endWrite(){
		numWriters--;
		notifyAll();
		
	}
}
