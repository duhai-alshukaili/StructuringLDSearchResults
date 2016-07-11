package com.wordpress.chapter10.evaluation;

import java.util.ArrayList;

import org.apache.commons.csv.CSVRecord;

public class ResultEntry {
	
	private String arguments;
	private boolean truthValue;
	private double score;
	private ArrayList<Object> orignalRecord;
	
	
	
	public ResultEntry(String arguments, boolean truthValue, double score, ArrayList<Object> recordData) {
		super();
		this.arguments = arguments;
		this.truthValue = truthValue;
		this.score = score;
		this.orignalRecord = recordData;
	}
	
	/**
	 * @return the arguments
	 */
	public String getArguments() {
		return arguments;
	}
	/**
	 * @param arguments the arguments to set
	 */
	public void setArguments(String arguments) {
		this.arguments = arguments;
	}
	/**
	 * @return the truthValue
	 */
	public boolean isTruthValue() {
		return truthValue;
	}
	/**
	 * @param truthValue the truthValue to set
	 */
	public void setTruthValue(boolean truthValue) {
		this.truthValue = truthValue;
	}
	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}
	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((arguments == null) ? 0 : arguments.hashCode());
		long temp;
		temp = Double.doubleToLongBits(score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (truthValue ? 1231 : 1237);
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
		if (!(obj instanceof ResultEntry)) {
			return false;
		}
		ResultEntry other = (ResultEntry) obj;
		if (arguments == null) {
			if (other.arguments != null) {
				return false;
			}
		} else if (!arguments.equals(other.arguments)) {
			return false;
		}
		if (Double.doubleToLongBits(score) != Double
				.doubleToLongBits(other.score)) {
			return false;
		}
		if (truthValue != other.truthValue) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ResultEntry [arguments=" + arguments + ", truthValue="
				+ truthValue + ", score=" + score + "]";
	}

	/**
	 * @return the orignalRecord
	 */
	public ArrayList<Object> getOrignalRecord() {
		return orignalRecord;
	}

	/**
	 * @param orignalRecord the orignalRecord to set
	 */
	public void setOrignalRecord(ArrayList<Object> orignalRecord) {
		this.orignalRecord = orignalRecord;
	}
	
	
	
}
