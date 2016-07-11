// FileName   : LiteralBean.java
// Date       : 20-10-2014
// Programmer : Duhai Alshukaili

package uk.ac.man.cs.rdb;


/**
 *
 * @author ispace
 */
public class LiteralBean {
    
    private Integer id;
    private String md5Hash;
    private String literalString;
    
    public LiteralBean() {}
    
    public LiteralBean(Integer id, String md5Hash, String literalString) {
        this.id = id;
        this.md5Hash = md5Hash;
        this.literalString = literalString;
    }
    
    

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((literalString == null) ? 0 : literalString.hashCode());
		result = prime * result + ((md5Hash == null) ? 0 : md5Hash.hashCode());
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
		if (!(obj instanceof LiteralBean)) {
			return false;
		}
		LiteralBean other = (LiteralBean) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (literalString == null) {
			if (other.literalString != null) {
				return false;
			}
		} else if (!literalString.equals(other.literalString)) {
			return false;
		}
		if (md5Hash == null) {
			if (other.md5Hash != null) {
				return false;
			}
		} else if (!md5Hash.equals(other.md5Hash)) {
			return false;
		}
		return true;
	}

	/**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the md5Hash
     */
    public String getMd5Hash() {
        return md5Hash;
    }

    /**
     * @param md5Hash the md5Hash to set
     */
    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    /**
     * @return the literalString
     */
    public String getLiteralString() {
        return literalString;
    }

    /**
     * @param literalString the literalString to set
     */
    public void setLiteralString(String literalString) {
        this.literalString = literalString;
    }

    /**
     * 
     * @return 
     */
    @Override
    public String toString() {
        return "LiteralBean{" + "id=" + id + ", md5Hash=" + md5Hash + ", literalString=" + literalString + '}';
    }
    
}
