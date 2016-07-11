package com.wordpress.chapter10.rec;



public class Tuple implements Comparable<Tuple>{
	
	private String entity1;
	private String entity2;
	private double score;
	private boolean same;
	
	
	public Tuple(String entity1, String entity2, double score, boolean same) {
		this.entity1 = entity1;
		this.entity2 = entity2;
		this.score = score;
		this.same = same;
	}


	@Override
	public int compareTo(Tuple that) {
		if (this.score < that.score)
			return -1;
		if (this.score > that.score)
			return 1;
		else 
			return 0;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Tuple [entity1=" + entity1 + ", entity2=" + entity2
				+ ", score=" + score + ", same=" + same + "]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity1 == null) ? 0 : entity1.hashCode());
		result = prime * result + ((entity2 == null) ? 0 : entity2.hashCode());
		result = prime * result + (same ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(score);
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
		if (!(obj instanceof Tuple)) {
			return false;
		}
		Tuple other = (Tuple) obj;
		if (entity1 == null) {
			if (other.entity1 != null) {
				return false;
			}
		} else if (!entity1.equals(other.entity1)) {
			return false;
		}
		if (entity2 == null) {
			if (other.entity2 != null) {
				return false;
			}
		} else if (!entity2.equals(other.entity2)) {
			return false;
		}
		if (same != other.same) {
			return false;
		}
		if (Double.doubleToLongBits(score) != Double
				.doubleToLongBits(other.score)) {
			return false;
		}
		return true;
	}


	/**
	 * @return the entity1
	 */
	public String getEntity1() {
		return entity1;
	}


	/**
	 * @param entity1 the entity1 to set
	 */
	public void setEntity1(String entity1) {
		this.entity1 = entity1;
	}


	/**
	 * @return the entity2
	 */
	public String getEntity2() {
		return entity2;
	}


	/**
	 * @param entity2 the entity2 to set
	 */
	public void setEntity2(String entity2) {
		this.entity2 = entity2;
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


	/**
	 * @return the same
	 */
	public boolean isSame() {
		return same;
	}


	/**
	 * @param same the same to set
	 */
	public void setSame(boolean same) {
		this.same = same;
	}

}
