package com.wordpress.chapter10.preprocess;


import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;

import resources.data.PrefixesMap;
import edu.princeton.cs.algs4.In;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;


public class PreprocessURISim {

	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			System.err
					.println("Usage: Preprocess collection_file.txt "
							+ "output_path");
			System.exit(1);
		}

		PrintUtil.registerPrefixMap(PrefixesMap.INSTANCE.load());


		String dataroot = args[1];

		File outDir = new File(dataroot);

		if (!outDir.exists() || !outDir.isDirectory()) {

			// attempt to create folder
			boolean success = (new File(dataroot)).mkdirs();
			if (!success) {
				System.err.println("[info]: Could not create output folder '"
						+ dataroot + ".");
				System.exit(1);
			}
		} else {
			System.err.println("[info]:  output folder '" + dataroot
					+ " already exists.");
		}

		In collectionIn = new In(args[0]);

		ArrayList<String> subjectObjectList = new ArrayList<String>();
		
		ArrayList<String> propertyList = new ArrayList<String>();
		
		ArrayList<String> typeList = new ArrayList<String>();

		// loop through the entire collection
		while (collectionIn.hasNextLine()) {
			String rdfFile = collectionIn.readLine();

			if (rdfFile.startsWith("//") || rdfFile.trim().length() == 0)
				continue;

			Model model = ModelFactory.createDefaultModel();

			model.read(rdfFile);

			// Resource centralResource =
			// SemanticWebTools.centralResource(model);

			// StmtIterator iter = model.listStatements(centralResource, null,
			// (RDFNode) null);

			StmtIterator iter = model.listStatements();

			while (iter.hasNext()) {

				Statement stmt = iter.next();

				// apply statements filter
				if (!valid(stmt))
					continue;

				Resource subject = stmt.getSubject();
				Property predicate = stmt.getPredicate();
				RDFNode object = stmt.getObject();

				// collect subjects & objects
				if (validSubject(subject)) {
					String s = PrintUtil.print(subject);

					if (!subjectObjectList.contains(s))
						subjectObjectList.add(s);
				}

				if (validObject(predicate, object)) {
					String o = PrintUtil.print(object);

					if (!subjectObjectList.contains(o))
						subjectObjectList.add(o);
				}

				// collect types
				if (predicate.getURI().equals(
						"http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
						|| predicate.getURI().equals(
						"http://rdf.freebase.com/ns/type.object.type")) {

					if (validTypeValue(object)) {
						String t = PrintUtil.print(object);

						if (!typeList.contains(t))
							typeList.add(t);
					}
				}

				// collect properties
				if (validPredicate(predicate)) {
					String p = PrintUtil.print(predicate);
					if (!propertyList.contains(p)) {
						propertyList.add(p);
					}

				}

			} // end while iter
		} // end while In

		
		
		ArrayList<SimData> recordList = new ArrayList<SimData>();

		for (String s1 : subjectObjectList) {
			for (String s2 : subjectObjectList) {
				
				SimData entry = new SimData(s1, s2, sim1(s1,s2));
				recordList.add(entry);
				//if (sim2(s1,s2) > 0.4)
				//printer.printRecord(s1, s2, sim2(s1,s2));
			}
		}
		
		
		for (String s1 : typeList) {
			for (String s2 : typeList) {
				
				SimData entry = new SimData(s1, s2, sim1(s1,s2));
				recordList.add(entry);
				//if (sim1(s1,s2) > 0.4)
				//printer.printRecord(s1, s2, sim1(s1,s2));
			}
		}
		
		for (String s1 : propertyList) {
			for (String s2 : propertyList) {
				
				SimData entry = new SimData(s1, s2, sim1(s1,s2));
				recordList.add(entry);
				//if (sim1(s1,s2) > 0.4)
				//printer.printRecord(s1, s2, sim1(s1,s2));
			}
		}
		
		
		Collections.sort(recordList);
		
		// write triple1 data
		CSVPrinter printer = new CSVPrinter(new FileWriter(dataroot
								+ "/SimURI.csv"), CSVFormat.DEFAULT);
	
		for (SimData rec: recordList) {
			
			if (rec.getSimilarity() > 0.35)
				printer.printRecord(rec.firstEntry, 
						            rec.secondEntry, 
						            rec.similarity);
		}
		printer.close();


	}

	private static boolean validPredicate(Property object) {
		if (object.isResource() && !object.isAnon()) {
			String objString = PrintUtil.print(object);

			if (objString.startsWith("rdf") || objString.startsWith("rdfs")
					|| objString.startsWith("owl")
					|| objString.startsWith("skos"))
				return false;
			else
				return true;
		} else {
			return false;
		}
	}

	private static boolean validTypeValue(RDFNode object) {
		if (object.isResource() && !object.isAnon()) {
			String objString = PrintUtil.print(object);

			if (objString.startsWith("rdf") || objString.startsWith("rdfs")
					|| objString.startsWith("owl")
					|| objString.startsWith("skos"))
				return false;
			else
				return true;
		} else {
			return false;
		}
	}

	private static boolean validObject(Property predicate, RDFNode object) {
		if (predicate.getURI().equals(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
				|| predicate.getURI().equals(
						"http://rdf.freebase.com/ns/type.object.type")) {
			return false;
		}

		if (object.isResource() && !object.isAnon()) {
			String objString = PrintUtil.print(object);

			if (objString.startsWith("rdf") || objString.startsWith("rdfs")
					|| objString.startsWith("owl")
					|| objString.startsWith("skos"))
				return false;
			else
				return true;
		} else {
			return false;
		}
	}

	private static boolean validSubject(Resource subject) {

		if (subject.isResource() && !subject.isAnon()) {
			String subjString = PrintUtil.print(subject);

			if (subjString.startsWith("rdf") || subjString.startsWith("rdfs")
					|| subjString.startsWith("owl")
					|| subjString.startsWith("skos"))
				return false;
			else
				return true;
		} else {
			return false;
		}

	}

	private static boolean valid(Statement stmt) {
		
		if (stmt.getSubject().isAnon() || stmt.getObject().isAnon())
			return false;

		
		if (stmt.getPredicate().toString().startsWith("http://dbpedia.org/property/"))
			return false;
		
		if (stmt.getPredicate().toString().equals("http://www.w3.org/2002/07/owl#sameAs") &&
		    stmt.getSubject().toString().contains("dbpedia.org/") &&
		    stmt.getObject().toString().contains("dbpedia.org"))
			return false;
		
		
		if (stmt.getObject().isLiteral()) {
			//String lang = stmt.getLanguage();
			
			//if (!lang.equals("")  && !lang.equals("en"))
			return false;
		}
		
		return true;
		
		/*
		if (stmt.getPredicate().toString()
				.startsWith("http://dbpedia.org/property/"))
			return false;

		if (stmt.getPredicate().toString()
				.equals("http://www.w3.org/2002/07/owl#sameAs")
				&& stmt.getSubject().toString().contains("dbpedia.org/")
				&& stmt.getObject().toString().contains("dbpedia.org"))
			return false;

		if (stmt.getObject().isLiteral()) {
			String lang = stmt.getLanguage();

			if (!lang.equals("") && !lang.equals("en"))
				return false;
		}

		return true;
		*/
	}

	/**
	 * Similarity between a types or predicates
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private static double sim1(String arg0, String arg1) {

		try {

			// create URI objects from arguments.
			// If any is not a URI then return 0.
			URI uri0 = (new ValueFactoryImpl()).createURI(arg0);
			URI uri1 = (new ValueFactoryImpl()).createURI(arg1);

			// if namespaces are the same, then compute exact similarity
			if (uri0.getNamespace().equals(uri1.getNamespace())) {
				if (uri0.getLocalName().equals(uri1.getLocalName())) {
					return 1.0;
				} else {
					return 0.0;
				}
			} else {
				String ln1 = uri0.getLocalName().trim();
				String ln2 = uri1.getLocalName().trim();
		
				// pre-porcess 
				ln1 = ln1.replaceAll("[0-9]", "");
				ln2 = ln2.replaceAll("[0-9]", "");

				if (ln1.length() == 0 || ln2.length() == 0)
					return 0.0;
		
				// return (new StoilosMetric()).getSimilarity(ln1, ln2);
				return (new Levenshtein()).getSimilarity(ln1, ln2);
				//return (new QGramsDistance()).getSimilarity(ln1, ln2);
			}
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace(System.err);
			return 0.0;
		}

	}

	/**
	 * Similarity between object uris
	 * 
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	private static double sim2(String arg0, String arg1) {

		try {

			// create URI objects from arguments.
			// If any is not a URI then return 0.
			URI uri0 = (new ValueFactoryImpl()).createURI(arg0);
			URI uri1 = (new ValueFactoryImpl()).createURI(arg1);

			String ln1 = uri0.getLocalName().trim();
			String ln2 = uri1.getLocalName().trim();

			if (ln1.length() == 0 || ln2.length() == 0)
				return 0.0;

			// return (new StoilosMetric()).getSimilarity(ln1, ln2);
			return (new Levenshtein()).getSimilarity(ln1, ln2);

		} catch (IllegalArgumentException ex) {
			ex.printStackTrace(System.err);
			return 0.0;
		}

	}

}
