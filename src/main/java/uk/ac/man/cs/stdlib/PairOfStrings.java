package uk.ac.man.cs.stdlib;



public class PairOfStrings implements Comparable<PairOfStrings>{
	
	private String first;
	private String second;
	
	public PairOfStrings(String first, String second) {
		this.setFirst(first);
		this.setSecond(second);
	}

	/**
	 * @return the second
	 */
	public String getSecond() {
		return second;
	}

	/**
	 * @param second the second to set
	 */
	public void setSecond(String second) {
		this.second = second;
	}

	/**
	 * @return the first
	 */
	public String getFirst() {
		return first;
	}

	/**
	 * @param first the first to set
	 */
	public void setFirst(String first) {
		this.first = first;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		//return "PairOfStrings [first=" + first + ", second=" + second + "]";
		return "[" + first + ", " + second + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PairOfStrings)) {
			return false;
		}
		PairOfStrings other = (PairOfStrings) obj;
		if (first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!first.equals(other.first)) {
			return false;
		}
		if (second == null) {
			if (other.second != null) {
				return false;
			}
		} else if (!second.equals(other.second)) {
			return false;
		}
		return true;
	}

	public int compareTo(PairOfStrings that) {
		if (this.equals(that))
			return 0;
		else {
			if (this.first.equals(that.first)) {
				return this.second.compareTo(that.second);
			} else
			{
				return this.first.compareTo(that.first);
			}
		}
	}
}
