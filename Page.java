
public class Page {
	int frameid; //page frame number
	int processid; //process number
	int vpage; //virtual page number
	int loadtime;
	int evicttime;
	int lutime; //last use time
	
	public Page(int n) {
		this.frameid = n;
	}
	
	public void allocate(Process p) {
		this.processid = p.id;
		this.vpage = p.refp;
		this.loadtime = Driver.time;
		this.lutime = Driver.time;
		
		p.curFrame.add(this);
	}
	
	public void evict() {
		evicttime = Driver.time;
		int pi = processid - 1;
		Process p = Driver.processlist.get(pi);
		p.residency += (evicttime - loadtime);
		p.eviction ++;
		p.curFrame.remove(this);
	}
 }
