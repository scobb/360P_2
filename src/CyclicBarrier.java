import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;


public class CyclicBarrier {
	private int numAcquired;
	private int parties;
	private Semaphore gateSemaphore;
	private Semaphore numAcquiredSemaphore;
	public CyclicBarrier(int parties){
		// Creates a new CyclicBarrier that will trip when
		// the given number of parties (threads) are waiting upon it
		numAcquired = 0;
		this.parties = parties;
		
		// Not bothering with a fair lock because we won't care about fairness
		gateSemaphore = new Semaphore(0);
		
		// a semaphore to guard the shared variable numAcquired
		numAcquiredSemaphore = new Semaphore(1);
	}
	int await() throws InterruptedException {
		// Waits until all parties have invoked await on this barrier.
		// If the current thread is not the last to arrive, then it is
		// disabled for thread scheduling purposes and lies dormant until
		// the last threa arrives.
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
			System.out.println("Acquiring...");
			gateSemaphore.acquire();
		}
		// sanity check: the first one we see here should ALWAYS be the largest index.
		System.out.println("I'm through, reuslt: " + result);
		
		// once we get here, ALL parties except the active one should be acquired (waiting)
		
		// this makes it cyclical: reset numAcquired
		numAcquired = 0;
		
		// release the lock -- increment to 1
		gateSemaphore.release();
		
		// return the original order we acquired the lock in
		return result;
	}
	// TODO - write real unit tests
	public static class CyclicBarrierTester extends Thread{
		private CyclicBarrier bar;
		private int alive = 0;
		public CyclicBarrierTester(CyclicBarrier bar){
			this.bar = bar;
		}
		public void run(){
			try {
				int result = this.bar.await();
				System.out.println("DAT RESULT: " + result);
			} catch (InterruptedException exc){
				System.out.println("EXCEPTION");
			}
		}
	}
	
	public static void main(String[] args){
		int numParties = 4;
		CyclicBarrier bar = new CyclicBarrier(numParties);
		List<CyclicBarrierTester> testers = new ArrayList<CyclicBarrierTester>();
		for (int i = 0; i < numParties; i++){
			testers.add(new CyclicBarrierTester(bar));
		}
		for (CyclicBarrierTester tester: testers){
			tester.start();
		}
	
	}

}
