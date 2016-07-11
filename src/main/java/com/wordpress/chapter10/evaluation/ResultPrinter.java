package com.wordpress.chapter10.evaluation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class ResultPrinter {

	public static void main(String[] args) {

		if (args.length < 2) {
			System.err.println("Usage: ResultPrinter RESULTS_DIR GT_DIR");
			System.exit(1);
		}

		// setup directories
		String resultsPath = args[0];
		String gtPath = args[1];
		String outPath = args[0];

		// double[] thresholds = {0, 0.1, 0.15, 0.2, 0.25, 0.30, 0.35, 0.40,
		// 0.45,
		// 0.50, 0.55, 0.60, 0.65, 0.70, 0.75, 0.80, 0.85, 0.90, 0.95, 1.0};

		// predicate to be evaluated
		String[] predicates = {"Entity", "Property", "EntityType", 
				"HasType", "HasProperty",
				"SimProperty", "SimEntity", "SimEntityType"};

		// number of arguments for each of the above predicate
		int[] argCounts = {1, 1, 1, 2, 2, 2, 2, 2};

		for (int i = 0; i < predicates.length; i++) {

			String pred = predicates[i];
			int argCount = argCounts[i];

			String predResultPath = resultsPath + "/" + pred + ".csv";
			String predGTPath = gtPath + "/GT_" + pred + ".csv";
			String predOut = outPath + "/" + pred + "_result.csv";

			// load the ground truth data
			HashMap<String, Double> groundTruthData = loadGTData(predGTPath,
					argCount);

			ArrayList<ResultEntry> resultData = loadResultData(pred,predResultPath,
					argCount, groundTruthData);
			
			if (resultData == null)
				continue;

			try {
				System.out.println("[info]: writing " + predOut);
				
				CSVPrinter printer = new CSVPrinter(new FileWriter(predOut),
						CSVFormat.DEFAULT);
				for (ResultEntry e : resultData) {

					
					int truth = 0;

					if (e.isTruthValue())
						truth = 1;
					
					ArrayList<Object> recordData = e.getOrignalRecord();
					//ArrayList<Object> recordData = new ArrayList<Object>();
					//for (String s: record)
					//	recordData.add(s);
					
					//recordData.add(new Double(e.getScore()));
					recordData.add(new Integer(truth));

					//printer.printRecord(e.getScore(), truth);
					printer.printRecord(recordData);
				}

				printer.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
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
	/*
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
	*/

	/**
	 * 
	 * @param resultData
	 * @return
	 */
	/*
	private static int countTotalTP(ArrayList<ResultEntry> resultData) {

		int count = 0;

		for (ResultEntry e : resultData) {
			if (e.isTruthValue()) {
				count++;
			}
		}
		return count;
	}
	*/

	/**
	 * 
	 * @param predResultPath
	 * @param argCount
	 * @param groundTruthData
	 * @return
	 */
	private static ArrayList<ResultEntry> loadResultData(String predicate, String predResultPath,
			int argCount, HashMap<String, Double> groundTruthData) {

		ArrayList<ResultEntry> resultsData = new ArrayList<ResultEntry>();
		//HashMap<String, ResultEntry> resultsDataMap = new HashMap<String, ResultEntry>();

		try {
			Iterable<CSVRecord> records = CSVFormat.DEFAULT
					.parse(new FileReader(predResultPath));

			for (CSVRecord record : records) {

				// don't consider Sim(x,x) in the evaluation
				if (argCount == 2 && predicate.startsWith("Sim") 
						&& record.get(0).equals(record.get(1)) )
					continue;
				
				String argumentString = buildArgumentsString(record, argCount);
				String reverseArgumentString = buildReverseArgumentsString(
						record, argCount);
				
				ArrayList<Object> recordData = new ArrayList<Object>();
				for (String s: record)
					recordData.add(s);

				ResultEntry entry = new ResultEntry(
						 argumentString
						,false
						,Double.parseDouble(record.get(argCount))
						,recordData);

				if (groundTruthData.containsKey(argumentString)) {
					if (groundTruthData.get(argumentString) == 1)
						entry.setTruthValue(true);
					else
						entry.setTruthValue(false);
				} else if (groundTruthData.containsKey(reverseArgumentString)) {
					if (groundTruthData.get(reverseArgumentString) == 1)
						entry.setTruthValue(true);
					else
						entry.setTruthValue(false);
				}

				else
					/*
					 * truth value is unknown if we have no GT about this entry
					 * default is false.
					 */
					;

				resultsData.add(entry);
				//resultsDataMap.put(argumentString, entry);
			}
			
			/*
			// append ground truth data that were not in results
			for (String argumentString: groundTruthData.keySet() ) {
				if (!resultsDataMap.containsKey(argumentString)) {
					System.out.println("Missing GT entry in results: " 
				             + argumentString);
					
					ArrayList<Object> recordData = new ArrayList<Object>();
					
					String arguments[] = argumentString.split("\\+");
					
					for (String s: arguments)
						recordData.add(s);
					
					recordData.add(new Double(0));
					//recordData.add(new Integer(1));
					
					ResultEntry entry = new ResultEntry(
							 argumentString
							,true
							,0
							,recordData);
					
					resultsData.add(entry);
				}
			}
			*/

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (Exception e) {
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
		
		//System.out.println(predGTPath);

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
