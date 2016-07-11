package uk.ac.man.cs.stdlib;

public class Similarity<T,C> implements Comparable<Similarity<T, C>>{

	private T first;
	private C second;
	private double sim;
	
	public Similarity(T first, C second, double sim) {
		this.setFirst(first);
		this.setSecond(second);
		this.setSim(sim);
	}
	
	
	public int compareTo(Similarity<T, C> o) {
		
		if (o.getSim()  < this.getSim())
			return -1;
		else if (this.getSim() < o.getSim())
			return 1;
		else return 0;
	}


	/**
	 * @return the first
	 */
	public T getFirst() {
		return first;
	}


	/**
	 * @param first the first to set
	 */
	public void setFirst(T first) {
		this.first = first;
	}


	/**
	 * @return the second
	 */
	public C getSecond() {
		return second;
	}


	/**
	 * @param second the second to set
	 */
	public void setSecond(C second) {
		this.second = second;
	}


	/**
	 * @return the sim
	 */
	public double getSim() {
		return sim;
	}


	/**
	 * @param sim the sim to set
	 */
	public void setSim(double sim) {
		this.sim = sim;
	}
}
