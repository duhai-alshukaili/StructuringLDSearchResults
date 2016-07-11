package com.wordpress.chapter10.preprocess;

public interface Literal<T> {
	
	public T getValue();
	public void setValue(T value);
	
	
	public String getID();
	public void setID(String id);
}
