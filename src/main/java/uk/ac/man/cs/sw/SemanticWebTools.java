package uk.ac.man.cs.sw;

import java.util.HashMap;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.FileDataSource;

import org.apache.any23.Any23;
import org.apache.any23.http.HTTPClient;
import org.apache.any23.source.DocumentSource;
import org.apache.any23.source.FileDocumentSource;
import org.apache.any23.source.HTTPDocumentSource;
import org.apache.any23.writer.NTriplesWriter;
import org.apache.any23.writer.TripleHandler;

public class SemanticWebTools {

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

	/**
	 * 
	 * @param uri
	 * @return
	 */
	public static String retrieveFromWeb(String uri) {
	

		Any23 runner = new Any23();
		runner.setHTTPUserAgent("net.ispace.");
		try {

			HTTPClient httpClient = runner.getHTTPClient();
			DocumentSource source = new HTTPDocumentSource(httpClient, uri);

			ByteArrayOutputStream out = new ByteArrayOutputStream();

			TripleHandler handler = new NTriplesWriter(out);

			runner.extract(source, handler);

			handler.close();

			String n3 = out.toString("UTF-8");

			// System.out.println(n3);
			if (n3.isEmpty()) {
				return null;
			}

			return n3;

		} catch (Throwable ex) {

			System.out.println("    **** Error when retrieving " + uri);

			Logger.getLogger(SemanticWebTools.class.getName()).log(
					Level.WARNING, null, ex);
		}

		// an exception occured
		return null;
	}
	
	/**
	 * 
	 * @param uri
	 * @return
	 */
	public static String retrieveFromDocument(String path, String uri) {
	

		Any23 runner = new Any23();
		
		try {
			DocumentSource source = new FileDocumentSource(new File(path), uri);

			ByteArrayOutputStream out = new ByteArrayOutputStream();

			TripleHandler handler = new NTriplesWriter(out);

			runner.extract(source, handler);

			handler.close();

			String n3 = out.toString("UTF-8");

			// System.out.println(n3);
			if (n3.isEmpty()) {
				return null;
			}

			return n3;

		} catch (Throwable ex) {

			System.out.println("    **** Error when retrieving " + uri);

			Logger.getLogger(SemanticWebTools.class.getName()).log(
					Level.WARNING, null, ex);
		}

		// an exception occured
		return null;
	}
	
	

}
