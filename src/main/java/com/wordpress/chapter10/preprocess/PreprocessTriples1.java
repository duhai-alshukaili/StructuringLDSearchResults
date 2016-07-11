package com.wordpress.chapter10.preprocess;

//import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
//import org.apache.commons.csv.CSVRecord;


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
//import uk.ac.man.cs.sw.SemanticWebTools;
import uk.ac.man.cs.stdlib.Triple;

public class PreprocessTriples1 {

	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			System.err.println("Usage: Preprocess collection_file.txt output_path");
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
		
		
		// store local names for predicates and types
		//HashMap<String, String> localNameMap = new HashMap<String, String>();

		// store triple1 pattern (uri,uri,uri)
		ArrayList<Triple> list1 = new ArrayList<Triple>();

		// store triple2 pattern (uri,uri,literal)
		//ArrayList<Triple> list2 = new ArrayList<Triple>();
		
		while (collectionIn.hasNextLine()) {
			String rdfFile = collectionIn.readLine();
			
			if (rdfFile.startsWith("//") || rdfFile.trim().length() == 0)
				continue;
			
			Model model = ModelFactory.createDefaultModel();

			model.read(rdfFile);
			
			//Resource centralResource = SemanticWebTools.centralResource(model);
			
			//StmtIterator iter = model.listStatements(centralResource, null, (RDFNode) null);
			
			StmtIterator iter = model.listStatements();

			while (iter.hasNext()) {

				Statement stmt = iter.next();

				// apply statements filter
				if (!valid(stmt)) continue;
				
				Resource subject = stmt.getSubject();
				Property predicate = stmt.getPredicate();
				RDFNode object = stmt.getObject();

				//localNameMap.put(PrintUtil.print(predicate),
				//		predicate.getLocalName());

		
				// check if this is a type predicate
				//if (predicate.getURI().equals(
				//		"http://www.w3.org/1999/02/22-rdf-syntax-ns#type") 
				//	|| predicate.getURI().equals(
				//		"http://rdf.freebase.com/ns/type.object.type")) {
					//localNameMap.put(PrintUtil.print(object), object.asResource()
					//		.getLocalName());
				//}

				Triple triple = null;
				if (!object.isLiteral()) {

					//String lang = stmt.getLanguage();
					//String value = object.asLiteral().getLexicalForm().trim();

					//if (value.length() > 0)
					//	triple = generateTriple(subject, predicate, object);

					//if (triple != null && !list2.contains(triple))
					//	list2.add(triple);
					
					triple = generateTriple(subject, predicate, object);

					if (triple != null && !list1.contains(triple))
						list1.add(triple);

				} 
			} // end while iter
		} // end while In

		

		// write triple1 data
		CSVPrinter printer = new CSVPrinter(new FileWriter(dataroot
				+ "/triple1.csv"), CSVFormat.DEFAULT);

		for (Triple t : list1) {
			printer.printRecord(t.getSubject(), t.getPredicate(), t.getObject());
		}

		printer.close();

		// write triple2 data
		//printer = new CSVPrinter(new FileWriter(dataroot + "/triple2.csv"),
		//		CSVFormat.DEFAULT);

		//for (Triple t : list2) {
		//	printer.printRecord(t.getSubject(), t.getPredicate(), t.getObject());
		//}

		//printer.close();

		// write local name data
		//printer = new CSVPrinter(new FileWriter(dataroot + "/localname.csv"),
		//		CSVFormat.DEFAULT);

		//for (String uri : localNameMap.keySet()) {
		//	printer.printRecord(uri, localNameMap.get(uri));
		//}

		//printer.close();

	}

	private static Triple generateTriple(Resource subject, Property predicate,
			RDFNode object) {

		String subString = PrintUtil.print(subject);
		String predString = PrintUtil.print(predicate);
		String objString;

		if (object.isResource()) {
			objString = PrintUtil.print(object);
		} else {

			// get the literal lexical form
			objString = object.asLiteral().getLexicalForm();

		}

		Triple t = new Triple(subString,predString,objString);


		return t;
		// return String.format("\"%s\",\"%s\",\"%s\"", subString, predString,
		// objString);

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
	}

}


