package com.wordpress.chapter10.feedback;

public class Entry2 {
	
	private String element1;
	private String element2;
	
	public Entry2(String e1, String e2) {
		this.element1 = e1;
		this.element2 = e2;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Entry2 [element1=" + element1 + ", element2=" + element2 + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((element1 == null) ? 0 : element1.hashCode());
		result = prime * result
				+ ((element2 == null) ? 0 : element2.hashCode());
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
		if (!(obj instanceof Entry2)) {
			return false;
		}
		Entry2 other = (Entry2) obj;
		if (element1 == null) {
			if (other.element1 != null) {
				return false;
			}
		} else if (!element1.equals(other.element1)) {
			return false;
		}
		if (element2 == null) {
			if (other.element2 != null) {
				return false;
			}
		} else if (!element2.equals(other.element2)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the element1
	 */
	public String getElement1() {
		return element1;
	}

	/**
	 * @param element1 the element1 to set
	 */
	public void setElement1(String element1) {
		this.element1 = element1;
	}

	/**
	 * @return the element2
	 */
	public String getElement2() {
		return element2;
	}

	/**
	 * @param element2 the element2 to set
	 */
	public void setElement2(String element2) {
		this.element2 = element2;
	}
	
	

}
