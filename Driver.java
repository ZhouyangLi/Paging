import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;


public class Driver {
	public static int time;
	
	public static int M; //machine size
	public static int P; //page size
	public static int S; //process size
	public static int J; //job mix
	public static int N; //number of reference for each process
	public static String R; //replacement algorithm
	
	public static int frameN; //number of frames
	
	
	public static LinkedList<Page> frametable_fifo = new LinkedList<Page>();
	//frame table updating in time manner
	public static LinkedList<Page> freeframe = new LinkedList<Page>();
	public static ArrayList<Page> frametable = new ArrayList<Page>();
	
	public static ArrayList<Process> processlist = new ArrayList<Process>();
	public static ArrayList<Process> waitlist = new ArrayList<Process>();
	
	public static int q = 3;
	
	
	public static void main(String[] args) throws FileNotFoundException {
		
		//get data from arguments
		if(args.length < 6) {
			System.err.println("Error: Incomplete argument.");
			System.exit(0);
		}
		M = Integer.parseInt(args[0]);
		P = Integer.parseInt(args[1]);
		S = Integer.parseInt(args[2]);
		J = Integer.parseInt(args[3]);
		N = Integer.parseInt(args[4]);
		R = args[5];
		
		
		//load random number file
		File file = new File("random-numbers.txt");
		if(!file.exists()) {
			System.err.println("Error: Cannot find random number file");
			System.exit(0);
		}
		if(!file.canRead()) {
			System.err.println("Error: Cannot read random number file");
			System.exit(0);
		}
		Scanner random = new Scanner(file);
		
		//print input info
		System.out.println("The machine size is " + M + ".");
		System.out.println("The page size is " + P + ".");
		System.out.println("The process size is " + S + ".");
		System.out.println("The job mix number is " + J + ".");
		System.out.println("The number of references per process is " + N + ".");
		System.out.println("The replacement algorithem is " + R + ".");
		if(args.length > 6) {
			System.out.println("The level of debugging output is " + args[6]);
		}
		
		//creating process
		if(J == 1) {
			Process p = new Process(1, P, S, N, 1, 0, 0);
			processlist.add(p);
			waitlist.add(p);
		}
		else if (J == 2) {
			for(int i = 1; i < 5; i++) {
				Process p = new Process(i, P, S, N, 1, 0, 0);
				processlist.add(p);
				waitlist.add(p);
			}
		}
		else if(J == 3) {
			for(int i = 1; i < 5; i++) {
				Process p = new Process(i, P, S, N, 0, 0, 0);
				processlist.add(p);
				waitlist.add(p);
			}
		}
		else if(J == 4) {
			Process p1 = new Process(1, P, S, N, 0.75, 0.25, 0);
			Process p2 = new Process(2, P, S, N, 0.75, 0, 0.25);
			Process p3 = new Process(3, P, S, N, 0.75, 0.125, 0.125);
			Process p4 = new Process(4, P, S, N, 0.5, 0.125, 0.125);
			processlist.add(p1);
			waitlist.add(p1);
			processlist.add(p2);
			waitlist.add(p2);
			processlist.add(p3);
			waitlist.add(p3);
			processlist.add(p4);
			waitlist.add(p4);
		}
		
		//initialize frame table
		frameN = M / P;
		for(int i = 0; i < frameN; i++) {
			//list of free frames
			Page f = new Page(i);
			freeframe.add(f);
			frametable.add(f);
		}
		
		

		//start to simulate each reference
		while(!waitlist.isEmpty()) {
			Process p = waitlist.remove(0);
			for(int ref = 0; ref < q; ref++) {
				if(p.refn > 0) {
					time ++;
					if(!p.pageFault()) {
						Page a = null;
						for(int i = 0; i < p.curFrame.size(); i++) {
							a = p.curFrame.get(i);
							if(a.vpage == p.refp) {
								break;
							}
						}
						a.lutime = time;
						
						
					}
					
					else {
						p.pagefault ++;
						if(freeframe.size()>0) {
							//if there are free frames, allocate p to the one with highest number
							Page frame = freeframe.removeLast();
							frame.allocate(p);
							frametable_fifo.add(frame);
							
						}
						else {
							if(R.equalsIgnoreCase("FIFO")) {
								replaceFIFO(p);
							}
							else if(R.equalsIgnoreCase("Random")) {
								replaceR(p, random);
							}
							else if(R.equalsIgnoreCase("LRU")) {
								replaceLRU(p);
							}
						}
					}
					
					
					//calculate the next reference for process p
					p.nextRef(random);
					p.refn --;
				}
				else{
					break;
				}
			}
			if(p.refn > 0) {
				waitlist.add(p);
			}
		}
		
		//print output
		int totalfault = 0;
		int totaleviction = 0;
		int totalresidency = 0;
		
		System.out.println();
		for(int i = 0; i < processlist.size();i++) {
			Process p = processlist.get(i);
			totalfault += p.pagefault;
			
			if(p.eviction == 0) {
				System.out.println("Process " + p.id + " has " + p.pagefault + " page faults.");
				System.out.println("	With no evictions, the average residency time is undefined.");
			}
			else{
				totaleviction += p.eviction;
				totalresidency += p.residency;
				double average = (double)p.residency/p.eviction;
				System.out.println("Process " + p.id + " has " + p.pagefault + " page faults and " +
						average + " average residency.");
			}
		}
		
		double taverage = (double)totalresidency/totaleviction;
		System.out.print("\nThe total number of faults is " + totalfault);
		if(totaleviction == 0) {
			System.out.println(".	With no evictions, the overall average residence is undefined.\n");
		}
		else{
			System.out.println(" and the overal average residency is " + taverage + ".");
		}
		
		
	}
	
	
	private static void replaceFIFO(Process p) {
		Page frame = frametable_fifo.removeFirst();
		frame.evict();
		frame.allocate(p);
		frametable_fifo.addLast(frame);
	}
	
	private static void replaceR(Process p, Scanner random) {
		int r = random.nextInt();
		int f = r % frameN;
		Page frame = frametable.get(f);
		frame.evict();
		frame.allocate(p);
	}
	
	private static void replaceLRU(Process p) {
		int temp = time;
		Page frame = null;
		for(Page pf: frametable) {
			if(pf.lutime < temp) {
				temp = pf.lutime;
				frame = pf;
			}
			
		}
			frame.evict();
			frame.allocate(p);
	}
}
