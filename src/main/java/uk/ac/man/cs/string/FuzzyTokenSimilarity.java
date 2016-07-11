// FileName: FuzzyTokenSimilarity
// Date    : 4-2-2015

package uk.ac.man.cs.string;

import java.util.ArrayList;

import uk.ac.man.cs.alg.MWBMatchingAlgorithm;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

/**
 * A class that implements the FuzzyTokenSimilarity matching which is described
 * in paper titled (Extending String Similarity Join to Tolerant Fuzzy Token
 * Matching) (2014)
 * 
 * @author ispace
 * 
 */
public class FuzzyTokenSimilarity {

	private String[] s1;
	private String[] s2;
	private AbstractStringMetric metric;
	double threshold;

	MWBMatchingAlgorithm matchingAlgoirthm;

	private FuzzyTokenSimilarity() {
	}
	
	
	/**
	 * 
	 * @param s1
	 * @param s2
	 * @param metric
	 * @param threshold
	 */
	public FuzzyTokenSimilarity(ArrayList<String> list1, ArrayList<String> list2,
			AbstractStringMetric metric, double threshold) {

		
		this.s1 = new String[list1.size()];
		this.s2 = new String[list2.size()];
		
		// copy the lists to an array
		this.s1 = list1.toArray(s1);
		this.s2 = list2.toArray(s2);
		
		this.metric = metric;
		this.threshold = threshold;

		matchingAlgoirthm = new MWBMatchingAlgorithm(this.s1.length, this.s2.length);

		fill();

	}

	/**
	 * 
	 * @param s1
	 * @param s2
	 * @param metric
	 * @param threshold
	 */
	public FuzzyTokenSimilarity(String[] s1, String[] s2,
			AbstractStringMetric metric, double threshold) {

		this.s1 = s1;
		this.s2 = s2;
		this.metric = metric;
		this.threshold = threshold;

		matchingAlgoirthm = new MWBMatchingAlgorithm(s1.length, s2.length);

		fill();

	}

	/**
	 * Populate the internal graph of the MatchingAlgoirthm object.
	 */
	private void fill() {

		for (int i = 0; i < s1.length; i++) {
			for (int j = 0; j < s2.length; j++) {

				float value = metric.getSimilarity(s1[i], s2[j]);

				if (value < threshold) {
					// akin to having to edge at all
					matchingAlgoirthm.setWeight(i, j, Double.NEGATIVE_INFINITY);
				} else {
					matchingAlgoirthm.setWeight(i, j, value);
				}
				
				//System.out.printf("%s,%s: %f\n", s1[i], s2[j], value);
			} // end for
		} // end for
	}

	/**
	 * Calculate the fuzzy Jaccard similarity of the two strings.
	 * 
	 * @return The fuzzyJaccard similarity of the two string arrays
	 */
	public double fuzzyJaccard() {
		
		double fuzzyWeight = totalMaxWeight();

		double value = fuzzyWeight / (s1.length + s2.length - fuzzyWeight);
		
		Double v = new Double(value);
		
		if (v.isNaN())
			return 0.0;
		else
			return value;

	}
	
	/**
	 * Calculate the fuzzy Dice similarity of the two strings.
	 * 
	 * @return The fuzzyDice similarity of the two string arrays
	 */
	public double fuzzyDice() {
		
		double fuzzyWeight = totalMaxWeight();

		double value = (2.0 * fuzzyWeight) / (s1.length + s2.length);
		
		Double v = new Double(value);
		
		if (v.isNaN())
			return 0.0;
		else
			return value;

	}
	
	
    /**
     * Calculate the total sum of the weights of the edges in the Maximal set of edges
     * produced by the matching algorithm
     * 
     * @return Sum of weights for the edges of the Maximal set of the bipartite graph.
     */
	private double totalMaxWeight() {

		int matching[] = matchingAlgoirthm.getMatching();

		double weightSum = 0.0;
		
		//System.out.println(matching.length);

		for (int i = 0; i < matching.length; i++) {
			// System.out.println(i);
			try {
				weightSum += matchingAlgoirthm.getWeight(i, matching[i]);
				//System.out.printf("%s - %s (%f)\n", s1[i], s2[matching[i]],matchingAlgoirthm.getWeight(i, matching[i]));
			} catch (ArrayIndexOutOfBoundsException ex) {
				//System.out.printf("%s - NA\n",s1[i]);
			} catch (java.lang.IllegalArgumentException ex) {
				//System.out.printf("%s - NA\n",s1[i]);
			}
		} // end for
		
		return weightSum;
	}

}
