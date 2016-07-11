package com.wordpress.chapter10.preprocess;

public class SimData implements Comparable<SimData> {

	String firstEntry;
	String secondEntry;
	double similarity;

	public SimData(String firstEntry, String secondEntry, double similarity) {
		super();
		this.firstEntry = firstEntry;
		this.secondEntry = secondEntry;
		this.similarity = similarity;
	}

	/**
	 * @return the firstEntry
	 */
	public String getFirstEntry() {
		return firstEntry;
	}

	/**
	 * @param firstEntry
	 *            the firstEntry to set
	 */
	public void setFirstEntry(String firstEntry) {
		this.firstEntry = firstEntry;
	}

	/**
	 * @return the secondEntry
	 */
	public String getSecondEntry() {
		return secondEntry;
	}

	/**
	 * @param secondEntry
	 *            the secondEntry to set
	 */
	public void setSecondEntry(String secondEntry) {
		this.secondEntry = secondEntry;
	}

	/**
	 * @return the similarity
	 */
	public double getSimilarity() {
		return similarity;
	}

	/**
	 * @param similarity
	 *            the similarity to set
	 */
	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	



	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((firstEntry == null) ? 0 : firstEntry.hashCode());
		result = prime * result
				+ ((secondEntry == null) ? 0 : secondEntry.hashCode());
		long temp;
		temp = Double.doubleToLongBits(similarity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		if (!(obj instanceof SimData)) {
			return false;
		}
		SimData other = (SimData) obj;
		if (firstEntry == null) {
			if (other.firstEntry != null) {
				return false;
			}
		} else if (!firstEntry.equals(other.firstEntry)) {
			return false;
		}
		if (secondEntry == null) {
			if (other.secondEntry != null) {
				return false;
			}
		} else if (!secondEntry.equals(other.secondEntry)) {
			return false;
		}
		if (Double.doubleToLongBits(similarity) != Double
				.doubleToLongBits(other.similarity)) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SimData [firstEntry=" + firstEntry + ", secondEntry="
				+ secondEntry + ", similarity=" + similarity + "]";

	}
	
	public int compareTo(SimData other) {
		
		if (this.similarity < other.similarity)
			return 1;
		else if (this.similarity > other.similarity)
			return -1;
		else 
			return 0;
		
	}

}
