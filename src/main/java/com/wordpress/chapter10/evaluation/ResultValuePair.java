package com.wordpress.chapter10.evaluation;

public class ResultValuePair {
	
	private boolean truth;
	private double score;
	
	

	public ResultValuePair(boolean truth, double score) {
		super();
		this.truth = truth;
		this.score = score;
	}

	
	
	


	/**
	 * @return the truth
	 */
	public boolean isTruth() {
		return truth;
	}





	/**
	 * @param truth the truth to set
	 */
	public void setTruth(boolean truth) {
		this.truth = truth;
	}





	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}





	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}









	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ResultValuePair [truth=" + truth + ", score=" + score + "]";
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (truth ? 1231 : 1237);
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
		if (!(obj instanceof ResultValuePair)) {
			return false;
		}
		ResultValuePair other = (ResultValuePair) obj;
		if (Double.doubleToLongBits(score) != Double
				.doubleToLongBits(other.score)) {
			return false;
		}
		if (truth != other.truth) {
			return false;
		}
		return true;
	}

	

}
