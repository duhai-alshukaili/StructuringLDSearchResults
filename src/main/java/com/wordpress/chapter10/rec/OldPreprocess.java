package com.wordpress.chapter10.rec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

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

public class OldPreprocess {

	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	static SET<String> entitySet = new SET<String>();
	static SET<String> propertySet = new SET<String>();
	static SET<String> propertyValueSet = new SET<String>();
	static SET<PairOfStrings> fromOntSet = new SET<PairOfStrings>();
	static SET<PairOfStrings> labelSet = new SET<PairOfStrings>();
	static SET<PairOfStrings> hasPropertyValueSet = new SET<PairOfStrings>();
	static SET<PairOfStrings> hasDomainSet = new SET<PairOfStrings>();

	public static void main(String[] args) {

		if (args.length < 3) {
			System.out.println("Usage: java Preprocess " + "[ONT_A] [ONT_B] [OUT_PATH]");

			System.exit(1);
		}

		String ontAPath = args[0];
		String ontBPath = args[1];
		String outPath = args[2];

		try {

			OWLOntology ontA = manager
					.loadOntologyFromOntologyDocument(new File(ontAPath));
			OWLOntology ontB = manager
					.loadOntologyFromOntologyDocument(new File(ontBPath));

			preprocessOnt(ontA, "A");
			preprocessOnt(ontB, "B");
			
			emit(entitySet, outPath + "/Entity.csv");
			emit(propertySet, outPath + "/Property.csv");
			emit(propertyValueSet, outPath + "/PropertyValue.csv");
			emitPairs(fromOntSet, outPath + "/FromOnt.csv");
			emitPairs(labelSet, outPath + "/Label.csv");
			emitPairs(hasPropertyValueSet, outPath + "/HasPropertyValue.csv");
			emitPairs(hasDomainSet, outPath + "/HasDomain.csv");

		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param o
	 */
	private static void preprocessOnt(OWLOntology ont, String label) {

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
		
		//if (label.equals("A")) System.out.println(individuals.size());

		//int count = 0;
		// loop through the individuals in the ontology
		for (OWLNamedIndividual ind : individuals) {
			//System.out.println(ind.toStringID());
			
		    //count++;
			
			//if (label.equals("A") && count > 133) break;

			entitySet.add(ind.toStringID());
			fromOntSet.add(new PairOfStrings(ind.toStringID(), label));

			for (OWLAnnotation annot : ind.getAnnotations(ont, rdfsLabel)) {
				String value = annot.getValue().toString();
				value = value.trim().replace("^^xsd:string", "").trim();
				value = StringUtils.strip(value.trim(), "\"");
				//System.out.println(value);

				labelSet.add(new PairOfStrings(ind.toStringID(), value));
				hasPropertyValueSet.add(new PairOfStrings(ind.toStringID(),
						value));
				propertyValueSet.add(value);
			}

			// loop through data property values
			Map<OWLDataPropertyExpression, Set<OWLLiteral>> valueMap = ind
					.getDataPropertyValues(ont);

			for (OWLDataPropertyExpression dpex : valueMap.keySet()) {
				//System.out
				//		.println("  " + dpex.asOWLDataProperty().toStringID());
				for (OWLLiteral literal : valueMap.get(dpex)) {
					//System.out.println("    " + literal.getLiteral());

					propertySet.add(dpex.asOWLDataProperty().toStringID());
					
					hasDomainSet.add(new PairOfStrings(literal.getLiteral(),
							dpex.asOWLDataProperty().toStringID()));
					
					
					hasPropertyValueSet.add(new PairOfStrings(ind.toStringID(),
							literal.getLiteral()));
					
					propertyValueSet.add(literal.getLiteral());

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
					
					propertySet.add(opex
							.asOWLObjectProperty().toStringID());
					
					hasDomainSet.add(new PairOfStrings(obj.toStringID(), opex
							.asOWLObjectProperty().toStringID()));
					
					hasPropertyValueSet.add(new PairOfStrings(ind.toStringID(),
							obj.toStringID()));
					
					propertyValueSet.add(obj.toStringID());

				}

			}
		}
	}

	/**
	 * 
	 * @param stringSet
	 * @param ptah
	 */
	private static void emitPairs(SET<PairOfStrings> pairSet, String path) {

		try {
			CSVPrinter printer = new CSVPrinter(new FileWriter(path),
					CSVFormat.DEFAULT);

			for (PairOfStrings pair : pairSet) {
				printer.printRecord(pair.getFirst(), pair.getSecond());
			}

			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param stringSet
	 * @param ptah
	 */
	private static void emit(SET<String> stringSet, String path) {

		try {
			CSVPrinter printer = new CSVPrinter(new FileWriter(path),
					CSVFormat.DEFAULT);

			for (String s : stringSet) {
				printer.printRecord(s);
			}

			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
