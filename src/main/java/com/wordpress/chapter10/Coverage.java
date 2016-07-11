package com.wordpress.chapter10;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.URI;

import resources.data.PrefixesMap;
import edu.princeton.cs.algs4.In;
import uk.ac.man.cs.stdlib.PairOfStrings;
import uk.ac.man.cs.string.StoilosMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;


public class Coverage {

	public static void main(String args[]) {

		if (args.length < 2) {
			System.err
					.println("Usage: Coverage RDF_COLLECTION_FILE ONTOLOGY_FOLDER");
			System.exit(1);
		}

		// load prefix map
		PrintUtil.registerPrefixMap(PrefixesMap.INSTANCE.load());

		String rdfCollectionFile = args[0];
		String ontologyFile = args[1];

		Set<String> dataSetTypes = dataSetConcepts(rdfCollectionFile);
		Set<String> ontologyConcepts = ontologyConcepts(ontologyFile);
		
	
		
		int conceptCount = 0;
		for (String type: dataSetTypes) {
			
			String matchingConcept = "";
			if ((matchingConcept = 
					findConceptMatch(type,ontologyConcepts)) != null) {
				
				System.out.println(type + " -- " + matchingConcept);
				
			    conceptCount++;
			}
		}
		
		System.out.println("Concepts: " + conceptCount);
		//System.out.println(conceptCount / (double)dataSetTypes.size());
		
		System.out.println("--------------------------");
		
		Set<String> dataSetProperties = dataSetProperties(rdfCollectionFile);
		Set<String> ontologyProperties = ontologyProperties(ontologyFile);
		
		
		int propertyCount = 0;
		
		for (String property: dataSetProperties) {
			
			String matchingProperty = "";
			
			if ((matchingProperty = findPropertyMatch(property, 
					ontologyProperties)) != null) {
				// System.out.println(property + " -- " + matchingProperty);
				
				propertyCount++;
			}
			
		}

		System.out.println("Properties: " + propertyCount);
		// System.out.println(propertyCount / (double)dataSetProperties.size());
		
		System.out.println("--------------------------");
		
		Set<PairOfStrings> eqConceptSet = EquivalentConcepts(ontologyFile);
		
		int count = 0;
		for (PairOfStrings p: eqConceptSet)
		{
			if (findConceptMatch(p.getFirst(), dataSetTypes) != null && 
				findConceptMatch(p.getSecond(), dataSetTypes) != null) {
				// System.out.println(p);
				count++;
			}
		}
		
		System.out.println("Eq Concepts: " + count);
		
		System.out.println("--------------------------");
		
		Set<PairOfStrings> domainSet = DomainAxioms(ontologyFile);
		
		count=0;
		for (PairOfStrings p: domainSet) {
			if (findPropertyMatch(p.getFirst(), dataSetProperties) != null && 
				findConceptMatch(p.getSecond(), dataSetTypes) != null) {
				 System.out.println(p);
				count++;
			}
		}
		
		System.out.println("Domain Axioms: " + count);

		
		System.out.println("--------------------------");
		
		Set<PairOfStrings> eqProperties = EquivalentProperties(ontologyFile);
		
		count=0;
		for (PairOfStrings p: eqProperties) {
			if (findPropertyMatch(p.getFirst(), dataSetProperties) != null && 
				findPropertyMatch(p.getSecond(), dataSetProperties) != null) {
				 //System.out.println(p);
				count++;
			}
		}
		
		System.out.println("Eq Properties: " + count);

		
		System.out.println("--------------------------");
		

		Set<PairOfStrings> disjointConceptSet = DisjointConcepts(ontologyFile);
		
		count=0;
		for (PairOfStrings p: disjointConceptSet)
		{
			if (findConceptMatch(p.getFirst(), dataSetTypes) != null && 
				findConceptMatch(p.getSecond(), dataSetTypes) != null) {
				 // System.out.println(p);
				count++;
			}
		}
		
		System.out.println("Disjoint Axioms: " + count);


	}

	/**
	 * 
	 * @param type
	 * @param ontologyConcepts
	 * @return
	 */
	private static String findConceptMatch(String type,
			Set<String> ontologyConcepts) {
		
		for (String oConcept: ontologyConcepts) {
			if (matchConcept(type,  oConcept)) {
				return oConcept;
			}
		}
		return null;
	}
	
	
	/**
	 * 
	 * @param property
	 * @param ontologyProperties
	 * @return
	 */
	private static String findPropertyMatch(String property, 
			Set<String> ontologyProperties) {
		
		for (String oProperty: ontologyProperties) {
			if (matchProperty(property, oProperty)) {
				return oProperty;
			}
		}
		
		return null;
	}

	/**
	 * 
	 * @param ontologyFile
	 * @return
	 */
	private static Set<String> ontologyProperties(String ontologyFolder) {
		Set<String> properties = new HashSet<String>();

		OntologyFilerFilter filter = new OntologyFilerFilter();

		File f = new File(ontologyFolder);

		for (File ontologyFile : f.listFiles(filter)) {

			Model model = ModelFactory.createDefaultModel();

			model.read(ontologyFile.getAbsolutePath());

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
								|| objectString
										.equals("owl:AnnotationProperty")
								|| objectString.equals("owl:ObjectProperty") || objectString
									.equals("owl:DatatypeProperty"))
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
	private static boolean matchConcept(String sc, String oc) {

		try {

			// create URI objects from arguments.
			// If any is not a URI then return 0.
			URI uri0 = (new ValueFactoryImpl()).createURI(sc);
			URI uri1 = (new ValueFactoryImpl()).createURI(oc);

			double sim = (new Levenshtein()).getSimilarity(uri0.getLocalName()
					.toLowerCase(), uri1.getLocalName().toLowerCase());
			if (sim >= 0.85) {
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
	 * @param sc
	 * @param oc
	 * @return
	 */
	private static boolean matchProperty(String sc, String oc) {

		try {

			// create URI objects from arguments.
			// If any is not a URI then return 0.
			URI uri0 = (new ValueFactoryImpl()).createURI(sc);
			URI uri1 = (new ValueFactoryImpl()).createURI(oc);

			double levenSim = (new Levenshtein()).getSimilarity(uri0.getLocalName()
					.toLowerCase(), uri1.getLocalName().toLowerCase());
			
			double stoilSim = (new StoilosMetric()).getSimilarity(uri0.getLocalName()
					.toLowerCase(), uri1.getLocalName().toLowerCase());
			
			double swSim = (new SmithWaterman()).getSimilarity(uri0.getLocalName()
					.toLowerCase(), uri1.getLocalName().toLowerCase());
			
			double sim = levenSim * 0.5 + stoilSim * 0.3 + swSim * 0.2;
			
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
	 * @param ontologyFolder
	 * @return
	 */
	private static Set<PairOfStrings> DisjointConcepts(String ontologyFolder) {
		Set<PairOfStrings> eqConcepts = new HashSet<PairOfStrings>();
		

		OntologyFilerFilter filter = new OntologyFilerFilter();

		File f = new File(ontologyFolder);

		for (File ontologyFile : f.listFiles(filter)) {

			Model model = ModelFactory.createDefaultModel();

			model.read(ontologyFile.getAbsolutePath());

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
				
				
				if (predicateString.equals("owl:disjointWith"))
					eqConcepts.add(new PairOfStrings(subjString, objectString));


			}
		}
		
		return eqConcepts;
	}
	
	/**
	 * 
	 * @param ontologyFolder
	 * @return
	 */
	private static Set<PairOfStrings> EquivalentConcepts(String ontologyFolder) {
		Set<PairOfStrings> eqConcepts = new HashSet<PairOfStrings>();
		

		OntologyFilerFilter filter = new OntologyFilerFilter();

		File f = new File(ontologyFolder);

		for (File ontologyFile : f.listFiles(filter)) {

			Model model = ModelFactory.createDefaultModel();

			model.read(ontologyFile.getAbsolutePath());

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
				
				
				if (predicateString.equals("owl:equivalentClass"))
					eqConcepts.add(new PairOfStrings(subjString, objectString));


			}
		}
		
		return eqConcepts;
	}
	
	/**
	 * 
	 * @param ontologyFolder
	 * @return
	 */
	private static Set<PairOfStrings> EquivalentProperties(String ontologyFolder) {
		Set<PairOfStrings> eqConcepts = new HashSet<PairOfStrings>();
		

		OntologyFilerFilter filter = new OntologyFilerFilter();

		File f = new File(ontologyFolder);

		for (File ontologyFile : f.listFiles(filter)) {

			Model model = ModelFactory.createDefaultModel();

			model.read(ontologyFile.getAbsolutePath());

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
				
				
				if (predicateString.equals("owl:equivalentProperty"))
					eqConcepts.add(new PairOfStrings(subjString, objectString));


			}
		}
		
		return eqConcepts;
	}
	
	
	
	
	
	
	/**
	 * 
	 * @param ontologyFolder
	 * @return
	 */
	private static Set<PairOfStrings> DomainAxioms(String ontologyFolder) {
		Set<PairOfStrings> eqConcepts = new HashSet<PairOfStrings>();
		

		OntologyFilerFilter filter = new OntologyFilerFilter();

		File f = new File(ontologyFolder);

		for (File ontologyFile : f.listFiles(filter)) {

			Model model = ModelFactory.createDefaultModel();

			model.read(ontologyFile.getAbsolutePath());

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
				
				
				if (predicateString.equals("rdfs:domain"))
					eqConcepts.add(new PairOfStrings(subjString, objectString));
			}
		}
		
		return eqConcepts;
	}
	

	/**
	 * 
	 * @param ontologyFile
	 * @return
	 */
	private static Set<String> ontologyConcepts(String ontologyFolder) {

		Set<String> concepts = new HashSet<String>();

		OntologyFilerFilter filter = new OntologyFilerFilter();

		File f = new File(ontologyFolder);

		for (File ontologyFile : f.listFiles(filter)) {

			Model model = ModelFactory.createDefaultModel();

			model.read(ontologyFile.getAbsolutePath());

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

class OntologyFilerFilter implements FileFilter {

	public OntologyFilerFilter() {
		super();
	}

	public boolean accept(File pathname) {

		String name = pathname.getName();

		if (name.lastIndexOf('.') > 0) {
			// get last index for '.' char
			int lastIndex = name.lastIndexOf('.');

			// get extension
			String str = name.substring(lastIndex);

			// match path name extension
			if (str.equals(".n3") 
					|| str.equals(".owl") 
					|| str.equals(".rdf")
					|| str.equals(".ttl")) {
				return true;
			}
		}
		return false;
	}

}
