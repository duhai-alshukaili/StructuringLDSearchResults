package com.wordpress.chapter10.preprocess;

public class DateLiteral implements Literal<Long>{

	private Long dateValue;
	
	private String dateString;
	
	private String id;
	
	
	public DateLiteral(String id, Long dateValue, String dateString) {
		this.setDateString(dateString);
		this.dateValue = dateValue;
		this.id = id;
	}
	
	public Long getValue() {
		
		return dateValue;
	}

	public void setValue(Long value) {
		this.dateValue = value;
		
	}

	public String getID() {
		// TODO Auto-generated method stub
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	/**
	 * @return the dateString
	 */
	public String getDateString() {
		return dateString;
	}

	/**
	 * @param dateString the dateString to set
	 */
	public void setDateString(String dateString) {
		this.dateString = dateString;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DateLiteral [dateValue=" + dateValue + ", dateString="
				+ dateString + ", id=" + id + "]";
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
		if (!(obj instanceof DateLiteral)) {
			return false;
		}
		DateLiteral other = (DateLiteral) obj;
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
