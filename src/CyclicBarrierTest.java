import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;


public class CyclicBarrierTest {
	private static List<Integer> results;
	
	private int testCommon(int numParties, int numRepeats){
		results = new ArrayList<Integer>();
		
		CyclicBarrier bar = new CyclicBarrier(numParties);
		for (int j = 0; j < numRepeats; j++){
			List<CyclicBarrierTester> testers = new ArrayList<CyclicBarrierTester>();
			for (int i = 0; i < numParties; i++){
				testers.add(new CyclicBarrierTester(bar));
			}
			for (CyclicBarrierTester tester: testers){
				tester.start();
			}
			for (CyclicBarrierTester tester: testers){
				try {
					tester.join();
				} catch (Exception exc){
					fail();
				}
			}
		}
		return results.size();
		
		
	}
	public static class CyclicBarrierTester extends Thread{
		private CyclicBarrier bar;
		private int alive = 0;
		public CyclicBarrierTester(CyclicBarrier bar){
			this.bar = bar;
		}
		public void run(){
			try {
				results.add(this.bar.await());
			} catch (InterruptedException exc){
				System.out.println("EXCEPTION");
			}
		}
	}

	
	@Test
	public void StandardTest(){
		int numParties = 4;
		assertEquals(numParties, testCommon(numParties, 1));
	}
	
	@Test
	public void RepeatTest() {
		int numParties = 4;
		int numRepeats = 4;
		
		assertEquals(numParties * numRepeats, testCommon(numParties, numRepeats));
	}
	
	@Test
	public void notEnoughTest(){
		results = new ArrayList<Integer>();
		int numParties = 4;
		CyclicBarrier bar = new CyclicBarrier(numParties);
		List<CyclicBarrierTester> testers = new ArrayList<CyclicBarrierTester>();
		for (int i = 0; i < numParties-1; i++){
			testers.add(new CyclicBarrierTester(bar));
		}
		for (CyclicBarrierTester tester: testers){
			tester.start();
		}
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(0, results.size());
	}
	
	@Test
	public void bigNumPartiesTest(){
		int numParties = 20;
		assertEquals(numParties, testCommon(numParties, 1));
		
	}
	@Test
	public void consecutiveEntrySingleTest(){
		int numParties = 4;
		results = new ArrayList<Integer>();
		
		CyclicBarrier bar = new CyclicBarrier(numParties);
		List<CyclicBarrierTester> testers = new ArrayList<CyclicBarrierTester>();
		List<CyclicBarrierTester> nextList = new ArrayList<CyclicBarrierTester>();
		for (int i = 0; i < numParties; i++){
			testers.add(new CyclicBarrierTester(bar));
		}
		for (CyclicBarrierTester tester: testers){
			tester.start();
		}
		for (CyclicBarrierTester tester: testers){
			try {
				tester.join();
				// create a new thread right away to try and get in

				if (tester == testers.get(3)) {
					CyclicBarrierTester next = new CyclicBarrierTester(bar);
					next.start();
				}
			} catch (Exception exc){
				fail();
			}
		}
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(4, results.size());
	}
	@Test
	public void consecutiveEntryTest(){
		int numParties = 4;
		results = new ArrayList<Integer>();
		
		CyclicBarrier bar = new CyclicBarrier(numParties);
		List<CyclicBarrierTester> testers = new ArrayList<CyclicBarrierTester>();
		List<CyclicBarrierTester> nextList = new ArrayList<CyclicBarrierTester>();
		for (int i = 0; i < numParties; i++){
			testers.add(new CyclicBarrierTester(bar));
		}
		for (CyclicBarrierTester tester: testers){
			tester.start();
		}
		for (CyclicBarrierTester tester: testers){
			try {
				tester.join();
				// create a new thread right away to try and get in
				CyclicBarrierTester next = new CyclicBarrierTester(bar);
				next.start();
				nextList.add(next);
			} catch (Exception exc){
				fail();
			}
		}
		
		for (CyclicBarrierTester tester: nextList){
			try {
				tester.join();
			} catch (Exception exc){
				fail();
			}
		}
		assertEquals(8, results.size());
	}

}
