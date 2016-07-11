package com.wordpress.chapter10.rec;

import edu.princeton.cs.algs4.SET;

public class Entity {
	
	private String ontLabel;
	
	private String id;
	
	private SET<String> propertyValues;
	
	public Entity(String id, String ontLabel) {
		this.id = id;
		this.ontLabel = ontLabel;
		propertyValues = new SET<String>();
	}

	/**
	 * @return the ontLabel
	 */
	public String getOntLabel() {
		return ontLabel;
	}

	/**
	 * @param ontLabel the ontLabel to set
	 */
	public void setOntLabel(String ontLabel) {
		this.ontLabel = ontLabel;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the propertyValues
	 */
	public SET<String> getPropertyValues() {
		return propertyValues;
	}
	
	public void addPropertyValue(String value) {
		propertyValues.add(value);
	}
}
