package com.wordpress.chapter10;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.openrdf.model.impl.ValueFactoryImpl;

import resources.data.PrefixesMap;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;
import com.wordpress.chapter10.util.MapUtil;

import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;
import edu.princeton.cs.algs4.SET;
import edu.princeton.cs.algs4.StdOut;

public class DatasetStats {

	public static void main(String args[]) {

		PrintUtil.registerPrefixMap(PrefixesMap.INSTANCE.load());

		Out fOut = new Out("data/dataset_stat/people.txt");

		String[] filePaths = {
				//"data/organizations/microsoft/microsoft_collection.txt",
				//"data/organizations/uk-university/uk_university_collection.txt",
				//"data/movies/casablanca/collection.txt",
				//"data/movies/the_god_father/collection.txt",
				//"data/movies/the_god_father_actors/collection.txt",
				//"data/cities/manchester/collection.txt",
				//"data/cities/berlin/collection.txt",
				"data/people/chris-bizer/christian-bizer_collection.txt",
				"data/people/tim_berners-lee/tim_berners-lee_collection.txt"
		};
		
		
		// load all the files in a single model
		Model model = ModelFactory.createDefaultModel();
		
		for (String path : filePaths) {
			In inputFile = new In(path);

			while (inputFile.hasNextLine()) {
				String rdfFile = inputFile.readLine();

				if (rdfFile.startsWith("//") || rdfFile.trim().length() == 0)
					continue;

				Model localModel = ModelFactory.createDefaultModel();

				model.read("file:" + rdfFile, "N-TRIPLE");

				model.add(localModel);
				
			} // end reading collection file

		} // end reading all files

		// keep the key stats. in these structures and counts
		Set<String> typeBag = new HashSet<String>();
		Set<String> propertyBag = new HashSet<String>();
		SET<String> subjectBag = new SET<String>();
		
		HashMap<String, Integer> typeMap = new HashMap<String,Integer>();
		HashMap<String, Integer> datasourceMap = new HashMap<String,Integer>();
		int tripleCount = 0;

		// lets iterate over the statements
		StmtIterator iter = model.listStatements();

		while (iter.hasNext()) {

			Statement stmt = iter.next();
			
			// apply statements filter
			if (!valid(stmt))
				continue;
			
			tripleCount++;

			if (isTypeTriple(stmt)) {

				String type = PrintUtil.print(stmt.getObject());
				String subject = PrintUtil.print(stmt.getSubject());
				
				if (!(type.startsWith("owl") || type.startsWith("rdf")
						|| type.startsWith("rdfs") || type
							.startsWith("skos"))) {
					
					subjectBag.add(subject);
					
					typeBag.add(type);
					
					if (typeMap.containsKey(type)) {
						typeMap.put(type, typeMap.get(type)+1);
					} else {
						typeMap.put(type, 1);
					}
					
				} // end if type
			}

			String predicate = PrintUtil.print(stmt.getPredicate());

			if (!(predicate.startsWith("owl")
					|| predicate.startsWith("rdf")
					|| predicate.startsWith("rdfs") || predicate
						.startsWith("skos")))
				propertyBag.add(predicate);

		} // end statement iterator

		
		//for (String type : typeBag) {
		//	System.out.println(type);
		//}
		
		

		fOut.println("Number of triples          : " + tripleCount);
		fOut.println("Number of types            : " + typeBag.size());
		fOut.println("Number of props            : " + propertyBag.size());
		fOut.println("Number of types Individuals: " + subjectBag.size());
		
		fOut.println("----------------------------------------");
		
		for (String subject: subjectBag) {
			String prefix = prefix(subject);
			
			if (datasourceMap.containsKey(prefix)) {
				datasourceMap.put(prefix, datasourceMap.get(prefix)+1);
			} else {
				datasourceMap.put(prefix, 1);
			}	
		}
		
		// sort and print
		
		Map<String, Integer> dsMap = 
						MapUtil.sortByValue(datasourceMap);
		
		for (Entry<String,Integer> entry: dsMap.entrySet()) {
			fOut.printf("%s,%s\n", entry.getKey(), entry.getValue());
		}
		
		fOut.println("----------------------------------------");
		
		
		// sort the values by keys
		Map<String, Integer> sortedTypeMap = 
					MapUtil.sortByValue(typeMap);
				
		for (Entry<String,Integer> entry: sortedTypeMap.entrySet()) {
			fOut.printf("%s,%s\n", entry.getKey(), entry.getValue());
		}
		fOut.println("----------------------------------------");
		
		//for (String type: typeBag) {
		//	StdOut.println(type);
		//}

	}

	private static boolean valid(Statement stmt) {

		if (stmt.getSubject().isAnon() || stmt.getObject().isAnon())
			return false;

		if (stmt.getPredicate().toString()
				.startsWith("http://dbpedia.org/property/"))
			return false;

		// filter the yagos
		if (stmt.getPredicate().toString()
				.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
				&& (stmt.getObject().toString()
						.startsWith("http://dbpedia.org/class/yago/") || stmt
						.getObject().toString()
						.startsWith("http://dbpedia.org/class/yago/")))
			return false;

		return true;
	}

	private static boolean isTypeTriple(Statement stmt) {

		if (stmt.getSubject().isAnon() || stmt.getObject().isAnon())
			return false;

		if (stmt.getPredicate().toString()
				.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
			return true;

		return false;

	}
	
	private static String prefix(String uri) {
		try {
			org.openrdf.model.URI uri0 = (new ValueFactoryImpl()).createURI(uri);
			return uri0.getNamespace();
		} catch (IllegalArgumentException ex) {
			return uri;
		}

	}
	
	

}




