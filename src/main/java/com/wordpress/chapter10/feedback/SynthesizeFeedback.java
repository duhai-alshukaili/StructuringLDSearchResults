package com.wordpress.chapter10.feedback;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;

public class SynthesizeFeedback {

	private static UniformIntegerDistribution dist = new UniformIntegerDistribution(
			1, 1000000);
	
	private static int flipCutOff = 100000;

	public static void main(String[] args) {

		if (args.length < 2) {
			System.out
					.println("Usage: java SynthesizeFeedback <GT_DIR> <OUT_DIR>");
			System.exit(1);
		}

		String gtPath = args[0];
		String outPath = args[1];
		int numOfUsers = 100;

		ArrayList<Entry1> entityType = loadFile1(gtPath + "/GT_EntityType.csv");
		Collections.shuffle(entityType);
		ArrayList<Feedback1> typeCorrectness = populateTypeCorrectness(
				entityType, numOfUsers);
		saveFeedback1(typeCorrectness, outPath + "/feedback1.csv");

		ArrayList<Entry2> hasType = loadFile2(gtPath + "/GT_HasType.csv");
		Collections.shuffle(hasType);
		ArrayList<Feedback2> typeMembership = populateTypeMembership(hasType,
				numOfUsers);
		saveFeedback2(typeMembership, outPath + "/feedback2.csv");

		ArrayList<Entry2> hasProperty = loadFile2(gtPath
				+ "/GT_HasProperty.csv");
		Collections.shuffle(hasProperty);
		ArrayList<Feedback2> propertyDomain = populatePropertyDomain(
				hasProperty, numOfUsers);
		saveFeedback2(propertyDomain, outPath + "/feedback2.csv");

		ArrayList<Entry2> simEntityType = loadFile2(gtPath
				+ "/GT_SimEntityType.csv");
		Collections.shuffle(simEntityType);
		ArrayList<Feedback2> typeEquiv = populatetypeEquiv(simEntityType,
				numOfUsers);
		saveFeedback2(typeEquiv, outPath + "/feedback2.csv");

		ArrayList<Entry2> simProp = loadFile2(gtPath + "/GT_SimProperty.csv");
		Collections.shuffle(simProp);
		ArrayList<Feedback2> propEquiv = populatePropEquiv(simProp, numOfUsers);
		saveFeedback2(propEquiv, outPath + "/feedback2.csv");

		ArrayList<Entry2> disjointType = //loadFile2(gtPath
				//+ "/GT_SimEntityType.csv");
                                       new ArrayList<Entry2>();
		
		disjointType.add(new Entry2("dbo:Person","dbo:Film"));
		disjointType.add(new Entry2("dbo:Person","dbo:Work"));
		disjointType.add(new Entry2("dbo:Person","wikidata:Q11424"));
		disjointType.add(new Entry2("dbo:Agent","dbo:Work"));
		disjointType.add(new Entry2("movie:actor","movie:film"));
		
		ArrayList<Feedback2> typedisjointness = populateTypeDisJointness(
				disjointType, numOfUsers);
		saveFeedback2(typedisjointness, outPath + "/feedback2.csv");

	}

	private static ArrayList<Feedback2> populateTypeDisJointness(
			ArrayList<Entry2> disjointType, int numOfUsers) {
		ArrayList<Feedback2> feedbackList = new ArrayList<Feedback2>();

		for (int i = 0; i < 5; i++) {

			for (int u = 1; u <= numOfUsers; u++) {

				Feedback2 feedback2;

				if (dist.sample() > flipCutOff)
					feedback2 = new Feedback2(
							disjointType.get(i).getElement1(), disjointType
									.get(i).getElement2(), "U" + u, "yes",
							"type_disjointness");
				else
					feedback2 = new Feedback2(
							disjointType.get(i).getElement1(), disjointType
									.get(i).getElement2(), "U" + u, "no",
							"type_disjointness");

				feedbackList.add(feedback2);
			}
		}

		return feedbackList;
	}

	public static void saveFeedback2(ArrayList<Feedback2> fbList,
			String filePath) {
		try {

			CSVPrinter printer = new CSVPrinter(new FileWriter(filePath, true),
					CSVFormat.DEFAULT);

			for (Feedback2 fb : fbList) {
				printer.printRecord(fb.getTerm1(), fb.getTerm2(), fb.getUser(),
						fb.getValue(), fb.getType());
			}

			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void saveFeedback1(ArrayList<Feedback1> fbList,
			String filePath) {
		try {

			CSVPrinter printer = new CSVPrinter(new FileWriter(filePath, true),
					CSVFormat.DEFAULT);

			for (Feedback1 fb : fbList) {
				printer.printRecord(fb.getTerm(), fb.getUser(), fb.getValue(),
						fb.getType());
			}

			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static ArrayList<Feedback2> populatePropEquiv(
			ArrayList<Entry2> simProp, int numOfUsers) {

		ArrayList<Feedback2> feedbackList = new ArrayList<Feedback2>();

		for (int i = 0; i < 5; i++) {

			for (int u = 1; u <= numOfUsers; u++) {

				Feedback2 feedback2;

				if (dist.sample() > flipCutOff)
					feedback2 = new Feedback2(simProp.get(i).getElement1(),
							simProp.get(i).getElement2(), "U" + u, "yes",
							"property_equivalence");
				else
					feedback2 = new Feedback2(simProp.get(i).getElement1(),
							simProp.get(i).getElement2(), "U" + u, "no",
							"property_equivalence");

				feedbackList.add(feedback2);
			}
		}

		return feedbackList;
	}

	private static ArrayList<Feedback2> populatetypeEquiv(
			ArrayList<Entry2> simEntityType, int numOfUsers) {
		ArrayList<Feedback2> feedbackList = new ArrayList<Feedback2>();

		for (int i = 0; i < 5; i++) {

			for (int u = 1; u <= numOfUsers; u++) {

				Feedback2 feedback2;

				if (dist.sample() > flipCutOff)
					feedback2 = new Feedback2(simEntityType.get(i)
							.getElement1(), simEntityType.get(i).getElement2(),
							"U" + u, "yes", "type_equivalence");
				else
					feedback2 = new Feedback2(simEntityType.get(i)
							.getElement1(), simEntityType.get(i).getElement2(),
							"U" + u, "no", "type_equivalence");

				feedbackList.add(feedback2);
			}
		}

		return feedbackList;
	}

	private static ArrayList<Feedback2> populatePropertyDomain(
			ArrayList<Entry2> hasProperty, int numOfUsers) {

		ArrayList<Feedback2> feedbackList = new ArrayList<Feedback2>();

		for (int i = 0; i < 5; i++) {

			for (int u = 1; u <= numOfUsers; u++) {

				Feedback2 feedback2;

				if (dist.sample() > flipCutOff)
					feedback2 = new Feedback2(hasProperty.get(i).getElement2(),
							hasProperty.get(i).getElement1(), "U" + u, "yes",
							"property_domain");
				else
					feedback2 = new Feedback2(hasProperty.get(i).getElement2(),
							hasProperty.get(i).getElement1(), "U" + u, "no",
							"property_domain");

				feedbackList.add(feedback2);
			}
		}

		return feedbackList;
	}

	private static ArrayList<Feedback2> populateTypeMembership(
			ArrayList<Entry2> hasType, int numOfUsers) {

		ArrayList<Feedback2> feedbackList = new ArrayList<Feedback2>();

		for (int i = 0; i < 5; i++) {
			for (int u = 1; u <= numOfUsers; u++) {

				Feedback2 feedback2;
				if (dist.sample() > flipCutOff)
					feedback2 = new Feedback2(hasType.get(i).getElement1(),
							hasType.get(i).getElement2(), "U" + u, "yes",
							"type_membership");
				else
					feedback2 = new Feedback2(hasType.get(i).getElement1(),
							hasType.get(i).getElement2(), "U" + u, "no",
							"type_membership");

				feedbackList.add(feedback2);
			}
		}

		return feedbackList;

	}

	private static ArrayList<Entry2> loadFile2(String path) {
		ArrayList<Entry2> entryList = new ArrayList<Entry2>();

		try {
			Iterable<CSVRecord> records = CSVFormat.DEFAULT
					.parse(new FileReader(path));

			for (CSVRecord record : records) {
				Entry2 entry = new Entry2(record.get(0), record.get(1));

				entryList.add(entry);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return entryList;
	}

	private static ArrayList<Feedback1> populateTypeCorrectness(
			ArrayList<Entry1> entityType, int numOfUsers) {

		ArrayList<Feedback1> feedbackList = new ArrayList<Feedback1>();

		for (int i = 0; i < 5; i++) {
			for (int u = 1; u <= numOfUsers; u++) {

				Feedback1 feedback1;
				if (dist.sample() > flipCutOff)
					feedback1 = new Feedback1(entityType.get(i).getElement(),
							"U" + u, "yes", "type_correctness");
				else
					feedback1 = new Feedback1(entityType.get(i).getElement(),
							"U" + u, "no", "type_correctness");

				feedbackList.add(feedback1);
			}
		}

		return feedbackList;
	}

	private static ArrayList<Entry1> loadFile1(String path) {

		ArrayList<Entry1> entryList = new ArrayList<Entry1>();

		try {
			Iterable<CSVRecord> records = CSVFormat.DEFAULT
					.parse(new FileReader(path));

			for (CSVRecord record : records) {
				Entry1 entry = new Entry1(record.get(0));

				entryList.add(entry);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return entryList;
	}

}
