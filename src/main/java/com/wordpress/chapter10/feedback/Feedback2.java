package com.wordpress.chapter10.feedback;

public class Feedback2 {
	
	private String term1;
	private String term2;
	private String user;
	private String value;
	private String type;
	
	public Feedback2(String term1, String term2, String user, String value, String type) {
		this.term1 = term1;
		this.term2 = term2;
		this.user = user;
		this.value = value;
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Feedback2 [term1=" + term1 + ", term2=" + term2 + ", user="
				+ user + ", value=" + value + ", type=" + type + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((term1 == null) ? 0 : term1.hashCode());
		result = prime * result + ((term2 == null) ? 0 : term2.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (!(obj instanceof Feedback2)) {
			return false;
		}
		Feedback2 other = (Feedback2) obj;
		if (term1 == null) {
			if (other.term1 != null) {
				return false;
			}
		} else if (!term1.equals(other.term1)) {
			return false;
		}
		if (term2 == null) {
			if (other.term2 != null) {
				return false;
			}
		} else if (!term2.equals(other.term2)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		if (user == null) {
			if (other.user != null) {
				return false;
			}
		} else if (!user.equals(other.user)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the term1
	 */
	public String getTerm1() {
		return term1;
	}

	/**
	 * @param term1 the term1 to set
	 */
	public void setTerm1(String term1) {
		this.term1 = term1;
	}

	/**
	 * @return the term2
	 */
	public String getTerm2() {
		return term2;
	}

	/**
	 * @param term2 the term2 to set
	 */
	public void setTerm2(String term2) {
		this.term2 = term2;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	

}
