package com.wordpress.chapter10.rec;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import org.apache.commons.lang3.StringUtils;
import com.wordpress.chapter10.util.MapUtil;
import edu.princeton.cs.algs4.StdOut;

public class BuildIndex {

	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();


	
	// ids for the instances
	private long id = 0;
	
	// id-Map
	private DualHashBidiMap<String, Long> idMap;
	
	// term postings
	private HashMap<String, Map<Long, Integer>> postings;

	// inverse document frequency for terms
	// in the collection
	private HashMap<String, Double> idf;

	/**
	 * @return the idf
	 */
	public HashMap<String, Double> getIdf() {
		return idf;
	}

	// term postings with TF-IDF weights
	private HashMap<String, Map<Long, Double>> tfIdf;

	

	// number of resources in the collection
	private int N;

	public BuildIndex(String ontAPath, String ontBPath) {

		idMap = new DualHashBidiMap<String, Long>();
		postings = new HashMap<String, Map<Long, Integer>>();
		tfIdf = new HashMap<String, Map<Long, Double>>();
		idf = new HashMap<String, Double>();
		N = 0;

		try {

			OWLOntology ontA = manager
					.loadOntologyFromOntologyDocument(new File(ontAPath));
			OWLOntology ontB = manager
					.loadOntologyFromOntologyDocument(new File(ontBPath));

			populatePostings(ontA);
			populatePostings(ontB);

			computeIdf();
			computeTFIDF();

		} catch (OWLOntologyCreationException e) {
			throw new ExceptionInInitializerError(e);
		}

	}

	/**
	 * pre-compute idf for all terms
	 */
	private void computeIdf() {
		for (String term : postings.keySet()) {
			idf.put(term, Math.log10((double) N / documentFrequncy(term)));
		}
	}

	/**
	 * create a postings structure with TF-IDF weights
	 */
	private void computeTFIDF() {

		for (String term : postings.keySet()) {
			Double idft = idf(term);

			Map<Long, Double> termDocuments = new HashMap<Long, Double>();

			for (Entry<Long, Integer> entry : postings.get(term).entrySet()) {
				termDocuments.put(entry.getKey(), idft * entry.getValue());
			}

			tfIdf.put(term, termDocuments);

		}
	}

	public double idf(String term) {
		if (!postings.containsKey(term))
			throw new IllegalArgumentException("Term: " + term
					+ " not in the collection");

		return idf.get(term);
	}

	/**
	 * 
	 * @param term
	 * @return
	 */
	public int documentFrequncy(String term) {

		if (!postings.containsKey(term))
			throw new IllegalArgumentException("Term: " + term
					+ " not in the collection");

		return postings.get(term).size();
	}

	/**
	 * 
	 * @param ont
	 * @param label
	 */
	private void populatePostings(OWLOntology ont) {

		OWLDataFactory fac = manager.getOWLDataFactory();

		OWLClass book = fac
				.getOWLClass(IRI
						.create("http://www.instancematching.org/ontologies/oaei2014#Book"));

		OWLAnnotationProperty rdfsLabel = fac.getOWLAnnotationProperty(IRI
				.create("http://www.w3.org/2000/01/rdf-schema#label"));

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();

		// ConsoleProgressMonitor progressMonitor = new
		// ConsoleProgressMonitor();

		OWLReasonerConfiguration config = new SimpleConfiguration();

		OWLReasoner reasoner = reasonerFactory.createReasoner(ont, config);

		NodeSet<OWLNamedIndividual> individualsNodeSet = reasoner.getInstances(
				book, true);

		Set<OWLNamedIndividual> individuals = individualsNodeSet.getFlattened();

		// if (label.equals("A")) System.out.println(individuals.size());

		// int count = 0;
		// loop through the individuals in the ontology
		for (OWLNamedIndividual ind : individuals) {
			// System.out.println(ind.toStringID());

			// count++;

			// if (label.equals("A") && count > 133) break;

			// entitySet.add(ind.toStringID());
			// fromOntSet.add(new PairOfStrings(ind.toStringID(), label));

			// a new indivdual found
			N++;

			String individualID = ind.toStringID();
			Long docId = (long) 0;
			if (getIdMap().containsKey(individualID)) {
				docId = getIdMap().get(individualID);
			} else {
				id++;
				docId = id;
				getIdMap().put(individualID, id);
			}

	

			for (OWLAnnotation annot : ind.getAnnotations(ont, rdfsLabel)) {
				String value = annot.getValue().toString();
				value = value.trim().replace("^^xsd:string", "").trim();
				value = StringUtils.strip(value.trim(), "\"");
				// System.out.println(value);

				updatePostings(tokeniz(value), docId);

				// labelSet.add(new PairOfStrings(ind.toStringID(), value));
				// hasPropertyValueSet.add(new PairOfStrings(ind.toStringID(),
				// value));
				// propertyValueSet.add(value);
			}

			// loop through data property values
			Map<OWLDataPropertyExpression, Set<OWLLiteral>> valueMap = ind
					.getDataPropertyValues(ont);

			for (OWLDataPropertyExpression dpex : valueMap.keySet()) {
				// System.out
				// .println("  " + dpex.asOWLDataProperty().toStringID());
				for (OWLLiteral literal : valueMap.get(dpex)) {
					// System.out.println("    " + literal.getLiteral());

					// propertySet.add(dpex.asOWLDataProperty().toStringID());

					// hasDomainSet.add(new PairOfStrings(literal.getLiteral(),
					// dpex.asOWLDataProperty().toStringID()));

					// hasPropertyValueSet.add(new
					// PairOfStrings(ind.toStringID(),
					// literal.getLiteral()));

					// propertyValueSet.add(literal.getLiteral());

					String value = literal.getLiteral();
					value = value.trim().replace("^^xsd:string", "").trim();
					value = StringUtils.strip(value.trim(), "\"");

					updatePostings(tokeniz(value), docId);

				}
			}

			// loop through object property values
			Map<OWLObjectPropertyExpression, Set<OWLIndividual>> objectMap = ind
					.getObjectPropertyValues(ont);

			for (OWLObjectPropertyExpression opex : objectMap.keySet()) {
				// System.out.println("  "
				// + opex.asOWLObjectProperty().toStringID());

				for (OWLIndividual obj : objectMap.get(opex)) {
					// System.out.println("    " + obj.toStringID());

					// propertySet.add(opex
					// .asOWLObjectProperty().toStringID());

					// hasDomainSet.add(new PairOfStrings(obj.toStringID(), opex
					// .asOWLObjectProperty().toStringID()));

					// hasPropertyValueSet.add(new
					// PairOfStrings(ind.toStringID(),
					// obj.toStringID()));

					String value = localName(obj.toStringID());
					updatePostings(tokeniz(value), docId);

					// propertyValueSet.add(obj.toStringID());

				}

			}
		}
	}

	/**
	 * 
	 * @param tokeniz
	 * @param documentID
	 */
	private void updatePostings(List<String> termList, Long documentID) {

		for (String term : termList) {

			if (postings.containsKey(term)) {
				/** the term already the postings map **/
				Map<Long, Integer> termDocuments = postings.get(term);

				// check if the saw the term in
				// this document before
				if (termDocuments.containsKey(documentID)) {
					// we saw this term in this document
					// update the count
					termDocuments.put(documentID,
							termDocuments.get(documentID) + 1);
				} else {
					// we see this term in this document for the first time
					termDocuments.put(documentID, 1);
				}

			} else {
				/** this is the first time we see the term **/

				// create a postings entry
				Map<Long, Integer> termDocuments = new HashMap<Long, Integer>();

				termDocuments.put(documentID, 1);

				// add the terms to the postings map
				postings.put(term, termDocuments);

			}

		}

	}

	/**
	 * 
	 */
	public void emitPostings() {

		Map<String, Double> sortedIdf = MapUtil.sortByValue(idf);

		for (String term : sortedIdf.keySet()) {

			StdOut.printf("[%s,%.3f] -->", term, idf.get(term));

			for (Entry<Long, Integer> entry : postings.get(term).entrySet()) {

				StdOut.printf("(%s, %d) ", entry.getKey(), entry.getValue());
			}

			StdOut.println("]");

		}
	}

	/**
	 * 
	 */
	public void emitTFIDFPostings() {

		Map<String, Double> sortedIdf = MapUtil.sortByValue(idf);

		for (String term : sortedIdf.keySet()) {

			if (tfIdf.get(term).size() > 1 && tfIdf.get(term).size() < 5) {
				
				StdOut.printf("[%s,%.3f] -->", term, idf.get(term));
				
				for (Entry<Long, Double> entry : tfIdf.get(term).entrySet()) {

					StdOut.printf("(%s, %.3f) ", getIdMap().getKey(entry.getKey()),
							entry.getValue());
				}

				StdOut.println("]");
			}

		}
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	private List<String> tokeniz(String value) {

		List<String> tokenList = new ArrayList<String>();
		value = value.replaceAll("[^a-zA-Z0-9]", " ");
		String tokens[] = value.split("\\s+");

		for (String t : tokens) {
			if (t.trim().equals(""))
				continue;
			tokenList.add(t.toLowerCase());
		}

		return tokenList;
	}

	/**
	 * 
	 * @param uri
	 * @return
	 */
	private static String localName(String uri) {
		try {
			org.openrdf.model.URI uri0 = (new ValueFactoryImpl())
					.createURI(uri);
			return uri0.getLocalName();
		} catch (IllegalArgumentException ex) {
			return uri;
		}

	}
	
	
	/**
	 * @return the tfIdf
	 */
	public HashMap<String, Map<Long, Double>> getTfIdf() {
		return tfIdf;
	}

	



	/**
	 * 
	 * @return
	 */
	public int numberOfDocuments() {
		return N;
	}
	
	/**
	 * @return the idMap
	 */
	public DualHashBidiMap<String, Long> getIdMap() {
		return idMap;
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 2) {
			System.out.println("Usage: java BuildIndex " + "[ONT_A] [ONT_B]");

			System.exit(1);
		}

		String ontAPath = args[0];
		String ontBPath = args[1];

		BuildIndex index = new BuildIndex(ontAPath, ontBPath);

		StdOut.println("The number of documents in the collection: "
				+ index.numberOfDocuments());

		index.emitTFIDFPostings();
	}

	

	

}
