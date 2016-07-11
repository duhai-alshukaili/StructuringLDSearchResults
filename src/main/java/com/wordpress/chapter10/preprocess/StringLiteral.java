package com.wordpress.chapter10.preprocess;

public class StringLiteral implements Literal<String>{

	private String id;
	private String value;
	
	public StringLiteral(String id, String value) {
		this.id = id;
		this.value = value;
	}
	
	public String getValue() {
		// TODO Auto-generated method stub
		return value;
	}

	public void setValue(String value) {
		this.value = value;
		
	}

	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StringLiteral [id=" + id + ", value=" + value + "]";
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
		if (!(obj instanceof StringLiteral)) {
			return false;
		}
		StringLiteral other = (StringLiteral) obj;
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
