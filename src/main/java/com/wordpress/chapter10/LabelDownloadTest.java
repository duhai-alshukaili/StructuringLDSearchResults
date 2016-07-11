package com.wordpress.chapter10;


import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

public class LabelDownloadTest {
	
	
	public static void main(String args[]) {
		
		
		Model model = ModelFactory.createDefaultModel();
		
		model.read("http://sws.geonames.org/287830/about.rdf");
		
		
		Resource centralResource = centralResource(model);
		
		
		
		StmtIterator iter = model.listStatements(centralResource, RDFS.label , (RDFNode)null);
		
		while(iter.hasNext()) {
			Statement stmt = iter.next();
			
			if (stmt.getObject().isLiteral()) {
				
				Literal literal = stmt.getLiteral();
				
				String lang = literal.getLanguage();
				
				if (!lang.equals("") && lang.startsWith("en"))
					System.out.println(literal.getLanguage() + ": " + literal.getLexicalForm());
				else if (lang.equals("")) { 
					System.out.println(literal.getLexicalForm());
				}
			}
		}
		
		
		//model.write(System.out, "N3");
	}
	
	/**
	 * Find the central resource of the given model. The central resources is a
	 * resource with the highest number of incoming/outgoing links
	 * 
	 * @param model
	 * @return
	 */
	public static Resource centralResource(Model model) {

		StmtIterator stmtIter = model.listStatements();

		HashMap<Resource, Integer> resourceMap = new HashMap<Resource, Integer>();

		while (stmtIter.hasNext()) {

			Statement stmt = stmtIter.next();

			// count the outgoing link for a resource
			Resource subject = stmt.getSubject();

			// update the count based on the subject resource
			if (resourceMap.containsKey(subject)) {
				resourceMap.put(subject, resourceMap.get(subject) + 1);
			} else {
				resourceMap.put(subject, 1);
			}

			// count the incoming link for a resource
			RDFNode object = stmt.getObject();

			// update the count based on the object resource
			if (object.isResource()) {

				if (resourceMap.containsKey(object.asResource())) {
					resourceMap.put(object.asResource(),
							resourceMap.get(object.asResource()) + 1);
				} else {
					resourceMap.put(object.asResource(), 1);
				}

			}

		} // end while stmt iter

		// no statements in the graph
		if (resourceMap.size() == 0)
			return null;

		// find the resource with max count
		Resource r = Collections.max(resourceMap.entrySet(),
				new Comparator<Map.Entry<Resource, Integer>>() {

					public int compare(Map.Entry<Resource, Integer> o1,
							Map.Entry<Resource, Integer> o2) {
						return o1.getValue() > o2.getValue() ? 1 : -1;
					}
				}).getKey();

		return r;
	}

}