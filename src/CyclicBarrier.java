import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;


public class CyclicBarrier {
	private int numAcquired, parties;
	private Semaphore gateSemaphore;
	private Semaphore numAcquiredSemaphore;
	public CyclicBarrier(int parties){
		// Creates a new CyclicBarrier that will trip when
		// the given number of parties (threads) are waiting upon it
		numAcquired = 0;
		this.parties = parties;
		// A fair lock to ensure no one can sneak ahead
		gateSemaphore = new Semaphore(0, true);
		
		// a semaphore to guard the shared variable numAcquired
		numAcquiredSemaphore = new Semaphore(1, true);
	}
	int await() throws InterruptedException {
		// Waits until all parties have invoked await on this barrier.
		// If the current thread is not the last to arrive, then it is
		// disabled for thread scheduling purposes and lies dormant until
		// the last thread arrives.
		// Returns: the arrival index of the current thread, where index
		// (parties - 1) indicates the first to arrive and zero indicates
		// the last to arrive
		
		//  be safe with the shared var
		numAcquiredSemaphore.acquire();
		
		// grab my result index
		int result = numAcquired;
		
		// increment the index for the next guy to read
		numAcquired++;
		
		// let others read shared var
		numAcquiredSemaphore.release();

		// we'll use an IF here because we're going to reset numAcquired after we get through.
		if (numAcquired != parties){
			// if we're not the last party here, acquire a lock
			//System.out.println("Acquiring...");
			gateSemaphore.acquire();
		}
		// sanity check: the first one we see here should ALWAYS be the largest index.
		//System.out.println("I'm through, result: " + result);
		
		// once we get here, ALL parties except the active one should be acquired (waiting)
		
		if (numAcquired == parties) {
			// reset numAcquired so the gate is cyclical
			numAcquired = 0;
			
			// release the lock for ready parties.
			gateSemaphore.release(parties - 1);
		}
		
		// return the original order we acquired the lock in
		return result;
	}
}
