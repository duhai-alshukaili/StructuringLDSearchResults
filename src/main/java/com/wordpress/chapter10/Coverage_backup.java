package com.wordpress.chapter10;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.URI;

import resources.data.PrefixesMap;
import edu.princeton.cs.algs4.In;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;

public class Coverage_backup {

	public static void main(String args[]) {

		if (args.length < 2) {
			System.err
					.println("Usage: Coverage RDF_COLLECTION_FILE ONTOLOGY_COLLECTION_FILE");
			System.exit(1);
		}

		// load prefix map
		PrintUtil.registerPrefixMap(PrefixesMap.INSTANCE.load());

		String rdfCollectionFile = args[0];
		String ontologyFile = args[1];

		Set<String> Cs = dataSetConcepts(rdfCollectionFile);

		Set<String> Co = ontologyConcepts(ontologyFile);
		
		Set<String> dsc = new HashSet<String>();

		int sum = 0;
		Set<String> matchedConcepts = new HashSet<String>();
		for (String sc : Cs) {
			
			

			for (String oc : Co) {

				if (match(sc, oc) && !matchedConcepts.contains(sc)) {
						
					//System.out.printf("[%s] match [%s]\n", sc,oc);
					sum += 1;
					matchedConcepts.add(sc);
					
				} 
					
			}
		}
		
		
		
		double cCoverage = ((double)sum) / Cs.size();
		int sum1 = sum;
		

	
		
		Set<String> Ps = dataSetProperties(rdfCollectionFile);
		Set<String> Po = ontologyProperties(ontologyFile);
		
		sum = 0;
		Set<String> matchedProps = new HashSet<String>();

		for (String sp : Ps) {

			dsc.add(sp);
			for (String op : Po) {

				if (match(sp, op) && !matchedProps.contains(sp)) {
					sum += 1;
					matchedProps.add(sp);
					//System.out.printf("[%s] match [%s]\n", sp,op);

				}
			}
		}
		
		double pCoverage = ((double)sum) / Ps.size();
		
		for (String s: dsc) {
			if (!matchedProps.contains(s)) {
				System.out.println(s);
			}
		}
		
		System.out.printf("Concept  Coverage (%d, %d) \n", sum1, Cs.size() );
		System.out.printf("Property  Coverage (%d, %d) \n", sum, Ps.size() );
		//System.out.println("Relation Coverage: " + pCoverage);

	}

	/**
	 * 
	 * @param ontologyFile
	 * @return
	 */
	private static Set<String> ontologyProperties(String ontologyFile) {
		Set<String> properties = new HashSet<String>();
		
		In collectionFile = new In(ontologyFile);
		
		while (collectionFile.hasNextLine()) {
			String file = collectionFile.readLine();
			
			Model model = ModelFactory.createDefaultModel();

			model.read(file);

			StmtIterator iter = model.listStatements();

			while (iter.hasNext()) {

				Statement stmt = iter.next();

				if (!valid(stmt))
					continue;

				Resource subject = stmt.getSubject();
				Property predicate = stmt.getPredicate();
				RDFNode object = stmt.getObject();

				String subjString = PrintUtil.print(subject);
				String predicateString = PrintUtil.print(predicate);
				String objectString = PrintUtil.print(object);

				if (predicateString.equals("rdf:type")
						&& (objectString.equals("rdf:Property") 
								|| objectString.equals("owl:AnnotationProperty")
								|| objectString.equals("owl:ObjectProperty")
								|| objectString.equals("owl:DatatypeProperty"))
						&& !subjString.startsWith("owl")
						&& !subjString.startsWith("rdfs")
						&& !subjString.startsWith("rdfs")
						&& !subjString.startsWith("skos")
						&& !subjString.startsWith("dbyago")
						&& !subjString.startsWith("yago"))
					properties.add(subjString);

			}
			
		}

		return properties;
	}

	/**
	 * 
	 * @param rdfCollectionFile
	 * @return
	 */
	private static Set<String> dataSetProperties(String rdfCollectionFile) {
		Set<String> properties = new HashSet<String>();

		In collectionIn = new In(rdfCollectionFile);

		while (collectionIn.hasNextLine()) {
			String rdfFile = collectionIn.readLine();

			if (rdfFile.startsWith("//") || rdfFile.trim().length() == 0)
				continue;

			Model model = ModelFactory.createDefaultModel();

			model.read(rdfFile);

			StmtIterator iter = model.listStatements();

			while (iter.hasNext()) {

				Statement stmt = iter.next();

				// apply statements filter
				if (!valid(stmt))
					continue;

				Resource subject = stmt.getSubject();
				Property predicate = stmt.getPredicate();
				RDFNode object = stmt.getObject();

				String predicateString = PrintUtil.print(predicate);
				String objectString = PrintUtil.print(object);

				if (!predicateString.startsWith("owl")
					&& !predicateString.startsWith("rdf")
					&& !predicateString.startsWith("rdfs")
					&& !predicateString.startsWith("skos"))
					properties.add(predicateString);

			} // end while iter
		} // end while In

		return properties;
	}

	/**
	 * 
	 * @param sc
	 * @param oc
	 * @return
	 */
	private static boolean match(String sc, String oc) {

		try {

			// create URI objects from arguments.
			// If any is not a URI then return 0.
			URI uri0 = (new ValueFactoryImpl()).createURI(sc);
			URI uri1 = (new ValueFactoryImpl()).createURI(oc);

			double sim =  (new Levenshtein()).getSimilarity(uri0.getLocalName().toLowerCase(), uri1.getLocalName().toLowerCase());
			if (sim >= 0.75) {
				return true;
			} else {
				return false;
			}

		} catch (IllegalArgumentException ex) {
			ex.printStackTrace(System.err);
			return false;
		}
	}

	/**
	 * 
	 * @param rdfCollectionFile
	 * @return
	 */
	private static Set<String> dataSetConcepts(String rdfCollectionFile) {

		Set<String> concepts = new HashSet<String>();

		In collectionIn = new In(rdfCollectionFile);

		while (collectionIn.hasNextLine()) {
			String rdfFile = collectionIn.readLine();

			if (rdfFile.startsWith("//") || rdfFile.trim().length() == 0)
				continue;

			Model model = ModelFactory.createDefaultModel();

			model.read(rdfFile);

			StmtIterator iter = model.listStatements();

			while (iter.hasNext()) {

				Statement stmt = iter.next();

				// apply statements filter
				if (!valid(stmt))
					continue;

				Resource subject = stmt.getSubject();
				Property predicate = stmt.getPredicate();
				RDFNode object = stmt.getObject();

				String predicateString = PrintUtil.print(predicate);
				String objectString = PrintUtil.print(object);

				if (predicateString.equals("rdf:type")
						&& !objectString.startsWith("owl")
						&& !objectString.startsWith("rdfs")
						&& !objectString.startsWith("dbyago")
						&& !objectString.startsWith("yago"))
					concepts.add(objectString);

			} // end while iter
		} // end while In

		return concepts;
	}

	/**
	 * 
	 * @param ontologyFile
	 * @return
	 */
	private static Set<String> ontologyConcepts(String ontologyFile) {

		Set<String> concepts = new HashSet<String>();
		
		In collectionFile = new In(ontologyFile);
		
		while (collectionFile.hasNextLine()) {
			
			String file = collectionFile.readLine();
			
			Model model = ModelFactory.createDefaultModel();

			model.read(file);

			StmtIterator iter = model.listStatements();

			while (iter.hasNext()) {

				Statement stmt = iter.next();

				if (!valid(stmt))
					continue;

				Resource subject = stmt.getSubject();
				Property predicate = stmt.getPredicate();
				RDFNode object = stmt.getObject();

				String subjString = PrintUtil.print(subject);
				String predicateString = PrintUtil.print(predicate);
				String objectString = PrintUtil.print(object);

				if (predicateString.equals("rdf:type")
						&& (objectString.equals("owl:Class") || objectString
								.equals("rdfs:Class"))
						&& !subjString.startsWith("owl")
						&& !subjString.startsWith("rdfs")
						&& !subjString.startsWith("rdfs")
						&& !subjString.startsWith("skos")
						&& !subjString.startsWith("dbyago")
						&& !subjString.startsWith("yago"))
					concepts.add(subjString);

			}
			
		}

		

		return concepts;
	}

	/**
	 * 
	 * @param stmt
	 * @return
	 */
	private static boolean valid(Statement stmt) {

		if (stmt.getSubject().isAnon() || stmt.getObject().isAnon())
			return false;

		if (stmt.getObject().isLiteral()) {
			return false;
		}

		if (stmt.getPredicate().toString()
				.startsWith("http://dbpedia.org/property/"))
			return false;

		if (stmt.getPredicate().toString()
				.equals("http://www.w3.org/2002/07/owl#sameAs")
				&& stmt.getSubject().toString().contains("dbpedia.org/")
				&& stmt.getObject().toString().contains("dbpedia.org"))
			return false;

		return true;
	}

}
