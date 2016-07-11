package uk.ac.man.cs.string;

import java.io.Serializable;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

public class StoilosMetric extends AbstractStringMetric implements Serializable {

	private String unmatchedA = "";
	private String unmatchedB = "";
	
	private float p = 0.6f;
	
	/**
	 * 
	 * @param p the importance of the difference factor (default 0.6)
	 */
	public StoilosMetric(float p) {
		this.p = p;
	}
	
	public StoilosMetric() {
		
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7178169051285401862L;

	@Override
	public String getShortDescriptionString() {
		// TODO Auto-generated method stub
		return "Stoilos";
	}

	@Override
	public String getLongDescriptionString() {
		// TODO Auto-generated method stub
		return "Implements the Stoilos similarity that specifically developed "
				+ "for ontology alignmnet, this metric explicity considers both "
		        + "the commonalities and the differences of the strings being compared";
	}

	@Override
	public String getSimilarityExplained(String string1, String string2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getSimilarityTimingEstimated(String string1, String string2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getSimilarity(String string1, String string2) {
		
		JaroWinkler jaro = new JaroWinkler();
		
		
		return mapRange(-1,1,0,1,comm(string1,string2) - diff(string1,string2)); 
				//+ jaro.getSimilarity(string1, string2);
	}

	@Override
	public float getUnNormalisedSimilarity(String string1, String string2) {
		// TODO Auto-generated method stub
		return getSimilarity(string1, string2);
	}
	
	private float comm(String string1, String string2) {
		StringBuffer aString = new StringBuffer(string1);
		StringBuffer bString = new StringBuffer(string2);
		
		String common = "";
		int sum = 0;
		
		do {
			common = LCSMetric.findLCS(aString.toString(), bString.toString());
			sum += common.length();
			
			aString.replace(aString.indexOf(common), 
					aString.indexOf(common) + common.length(), "");
			bString.replace(bString.indexOf(common), 
					bString.indexOf(common) + common.length(), "");
			
		} while (!common.equals(""));
		
		this.unmatchedA = aString.toString();
		this.unmatchedB = bString.toString();
		
		return (2 * sum) / (float) (string1.length() + string2.length());
	}
	
	
	private float diff(String string1, String string2) {
		float uLenA = unmatchedA.length() / (float) string1.length();
		float uLenB = unmatchedB.length() / (float) string2.length();
		
		return (uLenA * uLenB) / (this.p + (1-this.p) * 
				(uLenA + uLenB - uLenA * uLenB));
	}
	
	public static float mapRange(float a1, float a2, float b1, float b2, float s){
		return b1 + ((s - a1)*(b2 - b1))/(a2 - a1);
	}

}
