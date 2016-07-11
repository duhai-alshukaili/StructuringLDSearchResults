package uk.ac.man.cs.string;

import java.io.Serializable;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

public class LCSMetric extends AbstractStringMetric implements Serializable {


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8049937792007220865L;

	@Override
	public String getShortDescriptionString() {
		// TODO Auto-generated method stub
		return "Stoilos";
	}

	@Override
	public String getLongDescriptionString() {
		// TODO Auto-generated method stub
		return "Implements a similarity metric based on "
				+ "the longest commons substring";
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
		// TODO Auto-generated method stub
		return LCSMetric.findLCS(string1, string2).length() / 
				(float) string1.length();
	}

	@Override
	public float getUnNormalisedSimilarity(String string1, String string2) {
		// TODO Auto-generated method stub
		return getSimilarity(string1, string2);
	}

	/**
	 * Find the longest common substring of a and b
	 * 
	 * @param string1
	 *            The first string
	 * @param string2
	 *            The second string
	 * @return The longest common substring.
	 */
	public static String findLCS(String string1, String string2) {

		// figure out which string is shorter
		String shorter = string1;
		String longer = string2;

		if (string1.length() > string2.length()) {
			shorter = string2;
			longer = string1;
		}

		String lcs = "";

		// for each character in the shorter string
		for (int i = 0; i < shorter.length(); i++) {

			char c = shorter.charAt(i);
			int location = -1;
			String current = "";

			do {

				// check to see if the character is in the longer string
				location = longer.indexOf(c, location + 1);
				int temp = 0;

				// if it is, start from there and count the number of identical
				// characters
				for (int j = location; j < longer.length(); j++) {

					if ((i + temp) >= shorter.length() || location == -1) {
						break;
					}

					if (shorter.charAt(i + temp) == longer.charAt(j)) {
						current += shorter.charAt(i + temp);
					} else {
						break;
					}

					temp++;
				}

				if (current.length() > lcs.length()) {
					lcs = current;
				}
				current = "";

			} while (location != -1);
		}

		return lcs;
	}

}
