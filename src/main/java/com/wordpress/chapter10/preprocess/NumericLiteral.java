package com.wordpress.chapter10.preprocess;

public class NumericLiteral implements Literal<Double>{

	private Double value;
	
	private String numericString;
	
	private String id;
	
	
	public NumericLiteral(String id, Double value, String numericString) {
		
		this.numericString = numericString;
		this.value = value;
		this.id = id;
	}
	
	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
		
	}

	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}
	
	/**
	 * @return the dateString
	 */
	public String getNumericString() {
		return numericString;
	}

	/**
	 * @param dateString the dateString to set
	 */
	public void setNumericString(String numericString) {
		this.numericString = numericString;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "NumericLiteral [value=" + value + ", numericString="
				+ numericString + ", id=" + id + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (!(obj instanceof NumericLiteral)) {
			return false;
		}
		NumericLiteral other = (NumericLiteral) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}


}
