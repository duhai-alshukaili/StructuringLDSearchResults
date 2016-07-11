package com.wordpress.chapter10.test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import uk.ac.shef.wit.simmetrics.similaritymetrics.MongeElkan;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

import com.cybozu.labs.langdetect.*;



public class LangTest {
	
	
	static String path = "/home/ispace/Documents/programming/eclipse/"
			+ "LDPayGo2/data1/movies/the_god_father_actors/output";
	
	
	public static void main(String args[]) throws Exception {
		
		String namePath = path + "/Name.csv";
		String labelPath = path + "/Label.csv";
		String entityPath = path + "/Entity.csv";
		
		HashMap<String, ArrayList<String>>
			names = load(namePath);
		
		HashMap<String, ArrayList<String>>
			labels = load(labelPath);
		
		ArrayList<String> entities = load1(entityPath);
		
		for (String entityID: entities) {
			
			System.out.println(entityID);
			
			if (names.containsKey(entityID)) {
				if (names.get(entityID).size() == 1) {
					System.out.println("    " + names.get(entityID).get(0));
				} else {
					System.out.println("    " + chooseBest(entityID, names.get(entityID)));
				}
			} else if (labels.containsKey(entityID)) {
				if (labels.get(entityID).size() == 1) {
					System.out.println("    " + labels.get(entityID).get(0));
				} else {
					System.out.println("    " + chooseBest(entityID, labels.get(entityID)));
				}
			} else {
				System.out.println("    " + entityID);
			}
			
			
		}
		
	}
	
	private static String chooseBest(String entityID,
			ArrayList<String> arrayList) {
		URI uri0 = (new ValueFactoryImpl()).createURI(entityID);
		
		SmithWaterman swMetric = new SmithWaterman();
		MongeElkan meMetric = new MongeElkan();
		JaroWinkler jwMetric = new JaroWinkler();
		
		String localName = uri0.getLocalName();
		
	    double bestMatch = -1;
	    String bestString = "";
	    
	    for (String cand: arrayList) {
	    	double value = jwMetric.getSimilarity(localName, cand)
	    	             + meMetric.getSimilarity(localName, cand);
	    	
	    	if (value > bestMatch) {
	    		
	    		bestMatch = value;
	    		bestString = cand;
	    	}
	    }
	    
	    
		return bestString;
	}

	private static  ArrayList<String> load1(String namePath) 
			throws FileNotFoundException, IOException {
		
		Iterable<CSVRecord> records =
				CSVFormat.DEFAULT.parse(new FileReader(namePath));
		
		 ArrayList<String> names = new
				ArrayList<String>();
		
		for (CSVRecord record : records) {
			String entityID = record.get(0);
			names.add(entityID);			
		}
		
		return names;
	}


	private static HashMap<String, ArrayList<String>> load(String namePath) 
			throws FileNotFoundException, IOException {
		
		Iterable<CSVRecord> records =
				CSVFormat.DEFAULT.parse(new FileReader(namePath));
		
		HashMap<String, ArrayList<String>> names = 
				new HashMap<String, ArrayList<String>>();
		
		for (CSVRecord record : records) {
			String entityID = record.get(0);
			String name = record.get(1);
			
			if (names.containsKey(entityID)) {
				names.get(entityID).add(name);
			} else {
				names.put(entityID, new ArrayList<String>());
				names.get(entityID).add(name);
			}
		}
		
		return names;
	}

}
