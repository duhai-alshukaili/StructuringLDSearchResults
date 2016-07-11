package uk.ac.man.cs.stdlib;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

public class Triple {
	
	String subject;
	String predicate;
	String object;
	
	/**
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 */
	public Triple(String subject, String predicate, String object) {
		super();
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}
	
	

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result
				+ ((predicate == null) ? 0 : predicate.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
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
		if (!(obj instanceof Triple)) {
			return false;
		}
		Triple other = (Triple) obj;
		if (object == null) {
			if (other.object != null) {
				return false;
			}
		} else if (!object.equals(other.object)) {
			return false;
		}
		if (predicate == null) {
			if (other.predicate != null) {
				return false;
			}
		} else if (!predicate.equals(other.predicate)) {
			return false;
		}
		if (subject == null) {
			if (other.subject != null) {
				return false;
			}
		} else if (!subject.equals(other.subject)) {
			return false;
		}
		return true;
	}



	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}



	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}



	/**
	 * @return the predicate
	 */
	public String getPredicate() {
		return predicate;
	}



	/**
	 * @param predicate the predicate to set
	 */
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}



	/**
	 * @return the object
	 */
	public String getObject() {
		
		/*
		if (this.predicate.equals("rdf:type") && isVocabOrYago(this.object)) {
			return object;
		} else if (this.predicate.equals("rdf:type")){
			
			
			String localName = localName(this.object).toLowerCase();
			
			return localName.substring(0,1).toUpperCase() + 
					localName.substring(1);
		} else {
			return this.object;
		}
		*/
		return object;
		
	}



	private boolean isVocabOrYago(String object) {
		return ( object.startsWith("rdf:") || object.startsWith("rdfs:") ||
				 object.startsWith("owl:") ||  object.startsWith("skos:") ||
				 object.startsWith("dbyago:") || object.startsWith("yago:"));
		
	}
	
	
	private static String localName(String uri) {
		try {
			URI uri0 = (new ValueFactoryImpl()).createURI(uri);
			return uri0.getLocalName();
		} catch (IllegalArgumentException ex) {
			return uri;
		}

	}



	/**
	 * @param object the object to set
	 */
	public void setObject(String object) {
		this.object = object;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Triple [subject=" + subject + ", predicate=" + predicate
				+ ", object=" + object + "]";
	}
	
	
	

}
