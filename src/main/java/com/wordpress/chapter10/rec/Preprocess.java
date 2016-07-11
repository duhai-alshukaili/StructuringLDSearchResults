package com.wordpress.chapter10.rec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
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
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;

import uk.ac.man.cs.stdlib.PairOfStrings;
import edu.princeton.cs.algs4.SET;
import edu.princeton.cs.algs4.StdOut;

public class Preprocess {


	
	private ArrayList<Entity> list;
	
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	/**
	 * 
	 * @param o
	 */
	public Preprocess(String ontPath, String ontLabel) {
		
		list = new ArrayList<Entity>();
		
		OWLOntology ont;
		try {
			ont = manager
					.loadOntologyFromOntologyDocument(new File(ontPath));
		} catch (OWLOntologyCreationException e) {
			throw new ExceptionInInitializerError(e);
		}

		OWLDataFactory fac = manager.getOWLDataFactory();

		OWLClass book = fac
				.getOWLClass(IRI
						.create("http://www.instancematching.org/ontologies/oaei2014#Book"));

		OWLAnnotationProperty rdfsLabel = fac.getOWLAnnotationProperty(IRI
				.create("http://www.w3.org/2000/01/rdf-schema#label"));

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();


		OWLReasonerConfiguration config = new SimpleConfiguration();

		OWLReasoner reasoner = reasonerFactory.createReasoner(ont, config);

		NodeSet<OWLNamedIndividual> individualsNodeSet = reasoner.getInstances(
				book, true);

		Set<OWLNamedIndividual> individuals = individualsNodeSet.getFlattened();
		

		for (OWLNamedIndividual ind : individuals) {

			Entity entity = new Entity(ind.toStringID(), ontLabel);
			//entitySet.add(ind.toStringID());
			//fromOntSet.add(new PairOfStrings(ind.toStringID(), label));

			for (OWLAnnotation annot : ind.getAnnotations(ont, rdfsLabel)) {
				String value = annot.getValue().toString();
				value = value.trim().replace("^^xsd:string", "").trim();
				value = StringUtils.strip(value.trim(), "\"");
				//System.out.println(value);

				//labelSet.add(new PairOfStrings(ind.toStringID(), value));
				//hasPropertyValueSet.add(new PairOfStrings(ind.toStringID(),
				//		value));
				//propertyValueSet.add(value);
				
				entity.addPropertyValue(normalize(value));
				
				
			}

			// loop through data property values
			Map<OWLDataPropertyExpression, Set<OWLLiteral>> valueMap = ind
					.getDataPropertyValues(ont);

			for (OWLDataPropertyExpression dpex : valueMap.keySet()) {
				//System.out
				//		.println("  " + dpex.asOWLDataProperty().toStringID());
				for (OWLLiteral literal : valueMap.get(dpex)) {
					//System.out.println("    " + literal.getLiteral());

					//propertySet.add(dpex.asOWLDataProperty().toStringID());
					
					//hasDomainSet.add(new PairOfStrings(literal.getLiteral(),
					///		dpex.asOWLDataProperty().toStringID()));
					
					
					//hasPropertyValueSet.add(new PairOfStrings(ind.toStringID(),
					//		literal.getLiteral()));
					
					//propertyValueSet.add(literal.getLiteral());
					entity.addPropertyValue(normalize(literal.getLiteral()));

				}
			}

			// loop through object property values
			Map<OWLObjectPropertyExpression, Set<OWLIndividual>> objectMap = ind
					.getObjectPropertyValues(ont);

			for (OWLObjectPropertyExpression opex : objectMap.keySet()) {
				//System.out.println("  "
				//		+ opex.asOWLObjectProperty().toStringID());

				for (OWLIndividual obj : objectMap.get(opex)) {
					//System.out.println("    " + obj.toStringID());
					
					//propertySet.add(opex
					//		.asOWLObjectProperty().toStringID());
					
					//hasDomainSet.add(new PairOfStrings(obj.toStringID(), opex
					//		.asOWLObjectProperty().toStringID()));
					
					//hasPropertyValueSet.add(new PairOfStrings(ind.toStringID(),
					//		obj.toStringID()));
					
					//propertyValueSet.add(obj.toStringID());
					entity.addPropertyValue(
							normalize(localName(obj.toStringID())));

				} // end for

			}
			
			list.add(entity);
		}
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
	 * 
	 * @param uri
	 * @return
	 */
	private static String normalize(String uri) {
		String s1 = uri.replaceAll("[^a-zA-Z0-9]", " ");
		String s2 = s1.replaceAll("\\s+", " ");
		return s2;
	}


	/**
	 * @return the list
	 */
	public ArrayList<Entity> getList() {
		return list;
	}
	
	public static void main(String[] args) {

		if (args.length < 1) {
			System.out.println("Usage: java Preprocess " + "[ONT_A]");

			System.exit(1);
		}
		
		
		Preprocess preProcess = new Preprocess(args[0], "A");
		
		
		for (Entity e: preProcess.getList()) {
			StdOut.printf("%s\n", e.getId());
			
			for (String pv: e.getPropertyValues()) {
				StdOut.println(pv);
			}
			StdOut.println();
		}
		
		preProcess = new Preprocess(args[1], "B");
		
		for (Entity e: preProcess.getList()) {
			StdOut.printf("%s\n", e.getId());
			
			for (String pv: e.getPropertyValues()) {
				StdOut.println(pv);
			}
			StdOut.println();
		}

	}
}
