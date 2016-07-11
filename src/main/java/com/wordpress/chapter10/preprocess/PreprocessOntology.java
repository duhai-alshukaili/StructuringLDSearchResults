package com.wordpress.chapter10.preprocess;

//import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import uk.ac.man.cs.stdlib.Triple;

public class PreprocessOntology {

	public static void main(String args[]) {

		if (args.length < 2) {
			System.err
					.println("Usage: PreprocessOntology ontology_file full_output_path");
			System.exit(1);
		}

		PrintUtil.registerPrefixMap(PrefixesMap.INSTANCE.load());

		Model model = ModelFactory.createDefaultModel();

		model.read(args[0]);
		
		ArrayList<Triple> tripleList = new ArrayList<Triple>();

		StmtIterator iter = model.listStatements();

		while (iter.hasNext()) {

			Statement stmt = iter.next();

			if (!valid(stmt))
				continue;
			
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();
			
			Triple triple = generateTriple(subject, predicate, object);

			if (triple != null && !tripleList.contains(triple))
				tripleList.add(triple);
			
			// write triple1 data
			CSVPrinter printer;
			try {
				printer = new CSVPrinter(new FileWriter(args[1]), 
						CSVFormat.DEFAULT);
				
				for (Triple t : tripleList) {
					printer.printRecord(t.getSubject(), t.getPredicate(), t.getObject());
				}

				printer.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

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
	}

	private static boolean valid(Statement stmt) {

		if (stmt.getSubject().isAnon() || stmt.getObject().isAnon()
				|| stmt.getObject().isLiteral())
			return false;

		return true;
	}

}
