import java.util.ArrayList;
import java.util.Scanner;


public class Process {
	
	public int id; //process id
	public int size; //process size
	public int pagesize; 
	int refn; //number of reference
	double A; //probability of A
	double B; //probability of B
	double C; //probability of C
	int refa; //reference address
	int refp; //reference page
	int pagefault; //number of page fault
	int eviction; //number of evictions
	int residency;
	ArrayList<Page> curFrame = new ArrayList<Page>(); //list of current frames
	
	//constructor
	public Process(int num, int P, int S, int N, double A, double B, double C){
		this.id = num;
		this.pagesize = P;
		this.size = S;
		this.refn = N;
		this.A = A;
		this.B = B;
		this.C = C;
		
		this.refa = (111 * id) % this.size;
		this.refp = refa / pagesize;
		
		this.pagefault = 0;
		this.eviction = 0;
		this.residency = 0;
	}
	
	//calculate next reference address
	public int nextRef(Scanner random) {
		int r = random.nextInt();
		
		double y = r / (Integer.MAX_VALUE + 1d);
		if(y < A) {
			refa = (refa + 1) % size;
		}
		else if(y < A+B) {
			refa = (refa - 5 + size) % size;
		}
		else if(y < A+B+C) {
			refa = (refa + 4) % size;
		}
		else {
			//case random
			r = random.nextInt();
			refa = r % size;
		}

		refp = refa / pagesize;
		return this.refa;
	}
	

	
	//determine if page fault or not
	public boolean pageFault() {
		for(int i = 0; i < curFrame.size(); i++) {
			Page a = curFrame.get(i);
			if(a.vpage == refp)
				return false;
		}
		return true;
	}
}
