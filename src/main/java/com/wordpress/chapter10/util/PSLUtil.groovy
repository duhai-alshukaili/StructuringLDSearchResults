package com.wordpress.chapter10.util;

import java.io.File;
import java.io.PrintStream;
import java.io.FileWriter;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





import edu.umd.cs.psl.groovy.*;
import edu.umd.cs.psl.config.*;
import edu.umd.cs.psl.core.*;
import edu.umd.cs.psl.core.inference.*;
import edu.umd.cs.psl.ui.loading.*
import edu.umd.cs.psl.util.database.*
import edu.umd.cs.psl.evaluation.result.*;
import edu.umd.cs.psl.evaluation.resultui.printer.AtomPrintStream;
import edu.umd.cs.psl.evaluation.resultui.printer.DefaultAtomPrintStream;
import edu.umd.cs.psl.database.DataStore;
import edu.umd.cs.psl.database.Database;
import edu.umd.cs.psl.database.DatabasePopulator;
import edu.umd.cs.psl.database.DatabaseQuery;
import edu.umd.cs.psl.database.Partition;
import edu.umd.cs.psl.database.rdbms.RDBMSDataStore;
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver;
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver.Type;
import edu.emory.mathcs.utils.ConcurrencyUtils;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.predicate.Predicate;
import edu.umd.cs.psl.model.predicate.StandardPredicate;
import edu.umd.cs.psl.model.argument.GroundTerm;
import edu.umd.cs.psl.model.argument.StringAttribute;
import edu.umd.cs.psl.model.argument.Variable;
import edu.umd.cs.psl.model.atom.*;
import groovy.time.*;

class PSLUtil {
	static def loadPredicateAtoms(datastore, predicateMap, targetPartition){
		for (Predicate p : predicateMap.keySet() ){

			System.out.println("Loading files "+predicateMap[p]);
			InserterUtils.loadDelimitedData(datastore.getInserter(p,targetPartition),predicateMap[p]);
		}
	}

	static def loadPredicateAtomsWithValue(datastore, predicateMap, targetPartition){
		for (Predicate p : predicateMap.keySet() ){
			System.out.println("Loading files "+predicateMap[p]);
			InserterUtils.loadDelimitedDataTruth(datastore.getInserter(p,targetPartition),predicateMap[p]);
		}
	}


	static def loadPredicateAtoms(datastore, predicateMap, targetPartition, delemiter){
		for (Predicate p : predicateMap.keySet() ){

			System.out.println("Loading files "+predicateMap[p]);
			InserterUtils.loadDelimitedData(datastore.getInserter(p,targetPartition),predicateMap[p],delemiter);
		}
	}


	static def loadFromCSV(DataStore datastore, Map predicateMap, Partition targetPartition){
		for (Predicate p : predicateMap.keySet() ){

			System.out.println("Loading file: " + predicateMap[p]);
			//InserterUtils.loadDelimitedData(datastore.getInserter(p,targetPartition),predicateMap[p],delemiter);

			File inputFile = new File(predicateMap[p]);
			
			if (inputFile.exists() && !inputFile.isDirectory()) {
			
				Iterable<CSVRecord> records =
						CSVFormat.DEFAULT.parse(new FileReader(predicateMap[p]));
	
				def inserter = datastore.getInserter(p,targetPartition);
				for (CSVRecord record: records) {
					try {
						inserter.insert(record.values);
					} catch (java.lang.AssertionError ex) {
						
						System.err.println ("Duplicate: " + record.values + "\r\n");
						ex.printStackTrace(System.err);
					}
					
				} // end for CSVRecord
			
			} // end if
		
		} // end for Predicate
		
	} // end loadFromCSV
	
	/**
	 * 
	 * @param datastore
	 * @param predicateMap
	 * @param targetPartition
	 * @return
	 */
	static def loadFromCSVWithTruthValue(DataStore datastore, Map predicateMap, Partition targetPartition){
		for (Predicate p : predicateMap.keySet() ){
			
			System.out.println("Loading file: " + predicateMap[p]);

			// get the file group for this predicate
			List files = predicateMap[p];
			
			// declare an inserter for this file group
			def inserter = datastore.getInserter(p,targetPartition);
			
			files.each { file ->
				System.out.println("Loading file: " + file);
				
				Iterable<CSVRecord> records =
						CSVFormat.DEFAULT.parse(new FileReader(file.toString()));
				
				for (CSVRecord record: records) {
					
					
					try {
						int recordLength = record.values().length;
						String [] valuesCopy = new String[recordLength-1]; 
						
						// copy the predicate values
						for (int i=0; i < valuesCopy.length; i++) {
							valuesCopy[i] = record.values[i];
						}
						
						// truth value in the last column
						double truth = Double.parseDouble(record.values[recordLength-1]);
						
						// insert the data
						if (truth >= 0.2)
							inserter.insertValue(truth, valuesCopy);
						
					} catch (java.lang.AssertionError ex) {
						System.err.println ("Duplicate: " + record.values + "\r\n");
					}
				} // end for record
			} // end each		
			
		} // end for predicate map
	}

	/**
	 * 
	 * @param datastore
	 * @param predicateMap
	 * @param targetPartition
	 * @param delemiter
	 * @return
	 */
	static def loadPredicateAtomsWithValue(datastore, predicateMap, targetPartition, delemiter){
		for (Predicate p : predicateMap.keySet() ){
			System.out.println("Loading files "+predicateMap[p]);
			InserterUtils.loadDelimitedDataTruth(datastore.getInserter(p,targetPartition),predicateMap[p],delemiter);
		}
	}

	/**
	 * 
	 * @param datastore
	 * @param readPartition
	 * @param predicate
	 * @param out
	 * @return
	 */
	static def printResults(datastore, readPartition, predicate, PrintStream out){
		Partition dummy = new Partition(99);

		Database resultsDB = datastore.getDatabase(dummy, readPartition);

		Set atomSet = Queries.getAllAtoms(resultsDB,predicate);

		def resultsMap = [:]
		for (GroundAtom a : atomSet) {
			//out.println(atomToString(a))
			resultsMap.put(a,a.value);
		}

		Map sortedMap = resultsMap.sort {a, b -> b.value <=> a.value}

		sortedMap.each {atom, value ->
			out.println(atomToString(atom))
		}

		resultsDB.close();
	}


	/**
	 * Print the values of a predicate to a csv file. Each record in the 
	 * file contains two entries: A ground predicate and a value of the ground
	 * predicate between 0-1. The results are sorted in a descending order 
	 * based on the value of the predicates.
	 * 
	 * @param datastore The datastore that contains the partition
	 * @param readPartition The read partition that contain the atoms to be 
	 * 	results to be printed
	 * @param predicate The name of the predicate whose results are printed
	 * @param filePath The file path of the output CSV file
	 * @param printPredicateName. If true the predicate name will be printed in the result.
	 *        In such case the number of columns in the CSV file is going to be 2. Where
	 *        the first contains the predicate name enclosing the argumnets between '(' and ')'.
	 *        If false the predicate name will not be printed and each argumnet will be 
	 *        printed in its own cloumn.
	 * @return
	 */
	static def printCSVResults(DataStore datastore, Partition readPartition,
			Predicate predicate, String filePath,boolean printPredicateName = true) {

		Partition dummy = new Partition(99);

		Database resultsDB = datastore.getDatabase(dummy, readPartition);

		Set atomSet = Queries.getAllAtoms(resultsDB,predicate);

		def resultsMap = [:]
		for (GroundAtom a : atomSet) {
			//out.println(atomToString(a))
			resultsMap.put(a,a.value);
		}

		Map sortedMap = resultsMap.sort {a, b -> b.value <=> a.value}

		CSVPrinter printer = new CSVPrinter(new FileWriter(filePath, false),
				CSVFormat.DEFAULT);

		sortedMap.each {atom, value ->
			if (printPredicateName)
				printer.printRecord(predicateString(atom),value)
			else 
				printer.printRecord(argumentList(atom))
				
		}

		printer.close();
		resultsDB.close();

	}
	
	
	/**
	 * Print the values of a predicate to a csv file. Each record in the 
	 * file contains two entries: A ground predicate and a value of the ground
	 * predicate between 0-1. The results are sorted in a descending order 
	 * based on the value of the predicates.
	 * 
	 * @param datastore The datastore that contains the partition
	 * @param readPartition The read partition that contain the atoms to be 
	 * 	results to be printed
	 * @param predicate The name of the predicate whose results are printed
	 * @param filePath The file path of the output CSV file
	 * @param printPredicateName. If true the predicate name will be printed in the result.
	 *        In such case the number of columns in the CSV file is going to be 2. Where
	 *        the first contains the predicate name enclosing the argumnets between '(' and ')'.
	 *        If false the predicate name will not be printed and each argumnet will be 
	 *        printed in its own cloumn.
	 * @return
	 */
	static def printCSVResults(DataStore datastore, Partition readPartition,
			Predicate predicate, PrintStream out,boolean printPredicateName = true) {

		Partition dummy = new Partition(99);

		Database resultsDB = datastore.getDatabase(dummy, readPartition);

		Set atomSet = Queries.getAllAtoms(resultsDB,predicate);

		def resultsMap = [:]
		for (GroundAtom a : atomSet) {
			//out.println(atomToString(a))
			resultsMap.put(a,a.value);
		}

		Map sortedMap = resultsMap.sort {a, b -> b.value <=> a.value}

		CSVPrinter printer = new CSVPrinter(out,
				CSVFormat.DEFAULT);

		sortedMap.each {atom, value ->
			if (printPredicateName)
				printer.printRecord(predicateString(atom),value)
			else 
				printer.printRecord(argumentList(atom))
				
		}

		printer.close();
		resultsDB.close();

	}
	
	/**
	 * 
	 * @param db
	 * @return
	 */
	static def populateSingle(Database db, Predicate sourcePred, 
			Predicate targetPred) {
		
		// get the list of all subjects
		Set<GroundAtom> sourceAtomSet = Queries.getAllAtoms(db, sourcePred);
		Set<GroundTerm> termSet = new HashSet<GroundTerm>();
		
		for (GroundAtom atom : sourceAtomSet) {
			termSet.add(atom.getArguments()[0]);
		}
		
		/* Populates manually (as opposed to using DatabasePopulator) */
		for (GroundTerm term: termSet) {
			((RandomVariableAtom) db.getAtom(targetPred, term)).commitToDB();
		}
	}
	
	
	/**
	 * 
	 * @param db
	 * @return
	 */
	static def populatePair(Database db
			, Predicate source1Pred
			, Predicate source2Pred
			, Predicate targetPred) {
		
		// get the list of all subjects
		Set<GroundAtom> source1AtomSet = Queries.getAllAtoms(db, source1Pred);
		Set<GroundTerm> term1Set = new HashSet<GroundTerm>();
		
		Set<GroundAtom> source2AtomSet = Queries.getAllAtoms(db, source2Pred);
		Set<GroundTerm> term2Set = new HashSet<GroundTerm>();
		
		for (GroundAtom atom : source1AtomSet) {
			term1Set.add(atom.getArguments()[0]);
		}
		
		for (GroundAtom atom : source2AtomSet) {
			term2Set.add(atom.getArguments()[0]);
		}
		
		/* Populates manually (as opposed to using DatabasePopulator) */
		for (GroundTerm term1: term1Set) {
			for (GroundTerm term2: term2Set) {
				((RandomVariableAtom) db.getAtom(targetPred, term1, term2)).commitToDB();
			}
		}
	}
	
	
		
	/**
	 * 	
	 * @param atom The ground atom whose list of argumnets we are after.
	 * @return A list of strings containing the the agruments of the 
	 * given ground atom.
	 */
	private static ArrayList<String> argumentList(GroundAtom atom) {
		
		ArrayList<String> argsList = new ArrayList<String>();
		
		int numOfArgs = atom.arguments.length;
		
		for (int i=0; i < numOfArgs; i++) {
			if (atom.arguments[i] instanceof StringAttribute) {
			    argsList.add(atom.arguments[i].getValue());
			} else {
				argsList.add(atom.arguments[i].toString());
			}
		}
		
		argsList.add(Double.toString(atom.value));
		
		return argsList;
	}
		

	/**
	 * Convert a ground atom to string with the following format:
	 * 	PredicateName(arg1,arg2, ..., argN)
	 * @param atom
	 * @return
	 */
	private static String predicateString(GroundAtom atom) {
		StringBuilder sb = new StringBuilder();

		sb.append(atom.getPredicate().getName().toUpperCase()).append("(");

		int numOfArgs = atom.arguments.length;

		for (int i=0; i < numOfArgs - 1; i++) {
			//sb.append(atom.arguments[i].getValue()).append(", ");

			if (atom.arguments[i] instanceof StringAttribute) {
				sb.append(atom.arguments[i].getValue()).append(", ");
			} else {
				sb.append(atom.arguments[i].toString()).append(", ");
			}

		}

		//sb.append(atom.arguments[numOfArgs-1].getValue()).append(")");
		//sb.append(atom.arguments[numOfArgs-1].toString()).append(")");

		if (atom.arguments[numOfArgs-1] instanceof StringAttribute) {
			sb.append(atom.arguments[numOfArgs-1].getValue()).append(")");
		} else {
			sb.append(atom.arguments[numOfArgs-1].toString()).append(")");
		}


		return sb.toString();
	}

	/**
	 * Convert a ground atom to string with the following format:
	 * 	PredicateName(arg1,arg2, ..., argN) Score=[Prob. Value]
	 * @param atom
	 * @return
	 */
	private static String atomToString(GroundAtom atom) {
		StringBuilder sb = new StringBuilder();

		sb.append(atom.getPredicate().getName().toUpperCase()).append("(");

		int numOfArgs = atom.arguments.length;

		for (int i=0; i < numOfArgs - 1; i++) {
			sb.append(atom.arguments[i].getValue()).append(", ");
		}

		sb.append(atom.arguments[numOfArgs-1].getValue()).append(") Score=[");

		sb.append(atom.getValue()).append("]")

		return sb.toString();
	}
	
	/**
	 * 
	 * @return
	 */
	public static List dataSetList() {
		def myList =  ["actors", "chris_bizer", "manchester", "microsoft", "movies"] as List;
		
	}
	
	public static List targetPredicateList() {
		return ["Entity", "EntityType", "Property", "PropertyValue",
                "HasDomain", "HasType", "HasProperty", "HasPropertyValue",
				"SimPropertyValue", "SimEntityType", "SimEntity", "SimProperty"] as List;
	}
	
	/**
	 * 
	 * @param fileName
	 * @param numOfRules
	 * @return
	 */
	public static Map loadWeightMap(String fileName, int numOfRules, String prefix) {
		
		def weightMap = [:];
		
		int lineNo = 1;
		String line;
		
		// load the weight
		new File(fileName).withReader {reader ->
			while((line = reader.readLine()) != null) {
				double weight = Double.parseDouble(line);
				weightMap.put(prefix + lineNo.toString(), weight);
				if (lineNo == numOfRules)
					break;
				lineNo++;
			}
		}
		
		return weightMap;
	}
	
	/**
	 * 
	 * @param weight
	 * @param numOfRules
	 * @param prefix
	 * @return
	 */
	public static Map initializeWeightMap(double weight, double numOfRules, String prefix) {
		
		def weightMap = [:];
		
		for (int i=1; i <= numOfRules; i++) {
			weightMap.put(prefix + i , weight);
		}
		
		return weightMap;
	}
	
	public static printResults(DataStore dataStore, Partition partition, Map predicateFileMap) {
		predicateFileMap.each {predicate, file ->
			PSLUtil.printCSVResults(dataStore, partition, predicate, file, false);
		}
	}
	
	/**
	 * 
	 * @param db
	 * @return
	 */
	static def populateSingle(Database db, 
			Set<GroundTerm> termSet, Predicate targetPred) {		
		/* Populates manually (as opposed to using DatabasePopulator) */
		for (GroundTerm term: termSet) {
			((RandomVariableAtom) db.getAtom(targetPred, term)).commitToDB();
		}
	}
	
	
	/**
	 * 
	 * @param db
	 * @return
	 */
	static def populatePair(Database db
			, Set<GroundTerm> termSet1
			, Set<GroundTerm> termSet2
			, Predicate targetPred) {
		
		/* Populates manually (as opposed to using DatabasePopulator) */
		for (GroundTerm term1: termSet1) {
			for (GroundTerm term2: termSet2) {
				((RandomVariableAtom) db.getAtom(targetPred, term1, term2)).commitToDB();
			}
		}
	}
	

	/**
	 * 
	 * @param db
	 * @param pred
	 * @return
	 */
	static Set<GroundTerm> getGroundTermSet(Database db, Predicate pred) {
		
		Set<GroundAtom> atomSet = Queries.getAllAtoms(db, pred);
		Set<GroundTerm> termSet = new HashSet<GroundTerm>(); 
		
		for (GroundAtom atom : atomSet) {
			termSet.add(atom.getArguments()[0]);
		}
		
		return termSet;
	}
	
	/**
	 * 
	 * @param db
	 * @return
	 */
	static def populateSimPair(Database db
			, Set<GroundTerm> termSet
			, Predicate targetPred) {				
		
		//int count = 1;
		for (GroundTerm term1: termSet) {
			    //System.out.println("Grounding term #: " + count + ". "+ term1.toString());
				for (GroundTerm term2: termSet) {
					((RandomVariableAtom) db.getAtom(targetPred, term1, term2)).commitToDB();
				}
				//count++;
					
		}
	}
}

