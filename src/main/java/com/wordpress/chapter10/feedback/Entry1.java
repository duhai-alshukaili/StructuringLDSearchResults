package com.wordpress.chapter10.feedback;

public class Entry1 {
	
	private String element;
	
	public  Entry1(String e) {
		this.element = e;
	}
	
	

	/**
	 * @return the element
	 */
	public String getElement() {
		return element;
	}



	/**
	 * @param element the element to set
	 */
	public void setElement(String element) {
		this.element = element;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((element == null) ? 0 : element.hashCode());
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
		if (!(obj instanceof Entry1)) {
			return false;
		}
		Entry1 other = (Entry1) obj;
		if (element == null) {
			if (other.element != null) {
				return false;
			}
		} else if (!element.equals(other.element)) {
			return false;
		}
		return true;
	}
	
	

}
