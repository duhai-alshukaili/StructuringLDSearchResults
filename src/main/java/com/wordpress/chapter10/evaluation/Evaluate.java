package com.wordpress.chapter10.evaluation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class Evaluate {

	public static void main(String[] args) {

		if (args.length < 2) {
			System.err.println("Usage: Evaluate RESULTS_DIR GT_DIR");
			System.exit(1);
		}

		// setup directories
		String resultsPath = args[0];
		String gtPath = args[1];
		String outPath = args[0];

		double[] thresholds = {0, 0.1, 0.15, 0.2, 0.25, 0.30, 0.35, 0.40, 0.45,
				0.50, 0.55, 0.60, 0.65, 0.70, 0.75, 0.80, 0.85, 0.90, 0.95, 1.0};

		// predicate to be evaluated
		String[] predicates = {"Entity", "Property", "EntityType"};

		// number of arguments for each of the above predicate
		int[] argCounts = {1, 1, 1};

		for (int i = 0; i < predicates.length; i++) {

			String pred = predicates[i];
			int argCount = argCounts[i];

			String predResultPath = resultsPath + "/" + pred + ".csv";
			String predGTPath = gtPath + "/GT_" + pred + ".csv";
			String predOut = outPath + "/" + pred + ".out";

			// load the ground truth data
			HashMap<String, Double> groundTruthData = loadGTData(predGTPath,
					argCount);
			ArrayList<ResultEntry> resultData = loadResultData(predResultPath,
					argCount, groundTruthData);

			int total = resultData.size();
			int totalTP = countTotalTP(resultData);
			int totalTN = total - totalTP;

			System.out.println("Total: " + total);
			System.out.println("Total TP: " + totalTP);
			System.out.println("Total TN: " + totalTN);

			System.out.printf("%-15s %-5s %-5s %-5s %-5s %-7s %-7s\n",
					"Threshold", "TP", "FP", "FN", "TN", "PR", "RE");

			System.out.printf("%-15s %-5s %-5s %-5s %-5s %-7s %-7s\n",
					"---------", "-----", "-----", "-----", "-----", "-------",
					"-------");

			for (double threshold : thresholds) {

				int counts[] = count(resultData, threshold);

				double recall = ((double) counts[0]) / (counts[0] + counts[2]);
				double precision = ((double) counts[0])
						/ (counts[0] + counts[1]);

				System.out.printf(
						"%-15.2f %-5d %-5d %-5d %-5d %-7.3f %-7.3f\n",
						threshold, counts[0], counts[1], counts[2], counts[3],
						precision, recall);
			}

		}
	}

	/**
	 * 
	 * @param resultData
	 * @param threshold
	 * @return counts array: idx[0] => TP, idx[1] => FP, idx[2] => FN, idx[3] =>
	 *         TN
	 */
	private static int[] count(ArrayList<ResultEntry> resultData,
			double threshold) {

		// TP, FP, FN, TN
		int[] counts = {0, 0, 0, 0};

		for (ResultEntry e : resultData) {

			if (e.getScore() >= threshold && e.isTruthValue())
				counts[0]++;

			else if (e.getScore() >= threshold && !e.isTruthValue())
				counts[1]++;

			else if (e.getScore() < threshold && e.isTruthValue())
				counts[2]++;

			else if (e.getScore() < threshold && !e.isTruthValue())
				counts[3]++;

		}

		return counts;
	}

	/**
	 * 
	 * @param resultData
	 * @return
	 */
	private static int countTotalTP(ArrayList<ResultEntry> resultData) {

		int count = 0;

		for (ResultEntry e : resultData) {
			if (e.isTruthValue()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 
	 * @param predResultPath
	 * @param argCount
	 * @param groundTruthData
	 * @return
	 */
	private static ArrayList<ResultEntry> loadResultData(String predResultPath,
			int argCount, HashMap<String, Double> groundTruthData) {

		ArrayList<ResultEntry> resultsData = new ArrayList<ResultEntry>();

		try {
			Iterable<CSVRecord> records = CSVFormat.DEFAULT
					.parse(new FileReader(predResultPath));

			/*
			 * for (CSVRecord record : records) {
			 * 
			 * String argumentString = buildArgumentsString(record, argCount);
			 * String reverseArgumentString = buildReverseArgumentsString(
			 * record, argCount);
			 * 
			 * ResultEntry entry = new ResultEntry(argumentString, false,
			 * Double.
			 * parseDouble(record.get(argCount)),record.toMap().values());
			 * 
			 * if (groundTruthData.containsKey(argumentString)) { if
			 * (groundTruthData.get(argumentString) == 1)
			 * entry.setTruthValue(true); else entry.setTruthValue(false); }
			 * else if (groundTruthData.containsKey(reverseArgumentString)) { if
			 * (groundTruthData.get(reverseArgumentString) == 1)
			 * entry.setTruthValue(true); else entry.setTruthValue(false); }
			 * 
			 * else
			 * 
			 * ;
			 * 
			 * resultsData.add(entry);
			 * 
			 * }
			 */

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return resultsData;
	}

	/**
	 * 
	 * @param predGTPath
	 * @param argCount
	 * @return
	 */
	private static HashMap<String, Double> loadGTData(String predGTPath,
			int argCount) {

		HashMap<String, Double> groundTruthData = new HashMap<String, Double>();

		try {
			Iterable<CSVRecord> records = CSVFormat.DEFAULT
					.parse(new FileReader(predGTPath));

			for (CSVRecord record : records) {

				String entry = buildArgumentsString(record, argCount);

				groundTruthData.put(entry,
						Double.parseDouble(record.get(argCount)));

			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return groundTruthData;
	}

	/**
	 * 
	 * @param record
	 * @param argCount
	 * @return
	 */
	private static String buildArgumentsString(CSVRecord record, int argCount) {
		StringBuilder builder = new StringBuilder();

		// build a string out of the argCount
		for (int i = 0; i < argCount; i++) {
			builder.append(record.get(i)).append("+");
		}
		return builder.toString();
	}

	/**
	 * 
	 * @param record
	 * @param argCount
	 * @return
	 */
	private static String buildReverseArgumentsString(CSVRecord record,
			int argCount) {

		StringBuilder builder = new StringBuilder();

		// build a string out of the argCount
		for (int i = argCount - 1; i >= 0; i--) {
			builder.append(record.get(i)).append("+");
		}
		return builder.toString();
	}
}
