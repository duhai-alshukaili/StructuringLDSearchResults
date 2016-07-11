package com.wordpress.chapter10.test;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import uk.ac.man.cs.string.StoilosMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class URISimTest {

	public static void main(String args[]) {
		
		/*
		String[] uris = {
				"http://data.linkedmdb.org/resource/film/1234",
				"http://data.linkedmdb.org/data/film/3478",
		};
		
		for (String s: uris) {
			for (String t: uris) {
				System.out.printf("(%s, %s) = %s)\n\n", s, t, (new LexURISim()).getValue(s, t));
			}
		}
		*/
		
		String[] text = {
				"http://www.movieontology.org/2009/10/01/movieontology.owl#isCertificationOf",
				"The Space Trilogy locations"
		};
		
		for (String s: text) {
			System.out.println(getStringRepresentation(s));
		}

	}
	
	private static String getStringRepresentation(String arg) {
		
		try {
			// try to convert to a uri
			URI uri0 = (new ValueFactoryImpl()).createURI(arg);
			
			return normalize(uri0.getLocalName());
		} catch (IllegalArgumentException ex) {
			
			// if fail to convert to a URI return the actual input
			return normalize(arg);
		}	
	}
		
	private static String normalize(String arg) {
		
		StringBuilder builder = new StringBuilder();
		
		for (String s: arg.split("_") )
		{
			builder.append(s).append(" ");
		}
		
		return builder.toString().trim();
	}

}

class LexURISim {
	public double getValue(String arg0, String arg1) {

		
		
		try {
			
			// create URI objects from arguments.
			// If any is not a URI then return 0.
			URI uri0 = (new ValueFactoryImpl()).createURI(arg0);
			URI uri1 = (new ValueFactoryImpl()).createURI(arg1);
			
			String ln0 = uri0.getLocalName();
			String ln1 = uri1.getLocalName();
			
			if (ln0.equals(ln1)) {
				return 1.0;
			}

			String cleanL0 = ln0.replaceAll("[0-9]", "");
			String cleanL1 = ln1.replaceAll("[0-9]", "");
			
			// throw away stuff that has dominating digits
			if ( (ln0.length() - cleanL0.length()) >= cleanL0.length() )
				return 0.0;
				
			if ( (ln1.length() - cleanL1.length()) >= cleanL1.length() )
				return 0.0;
				
			return (new Levenshtein()).getSimilarity(cleanL0, cleanL1);
				
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace(System.err);
			return 0.0;
		}
	}
	
	public  double sim1(String arg0, String arg1) {

		try {

			// create URI objects from arguments.
			// If any is not a URI then return 0.
			URI uri0 = (new ValueFactoryImpl()).createURI(arg0);
			URI uri1 = (new ValueFactoryImpl()).createURI(arg1);
			
			System.out.println(uri0.getNamespace());
			System.out.println(uri1.getNamespace());

			// if namespaces are the same, then compute exact similarity
			if (uri0.getNamespace().equals(uri1.getNamespace())) {
				if (uri0.getLocalName().equals(uri1.getLocalName())) {
					return 1.0;
				} else {
					return 0.0;
				}
			} else {
				String ln0 = uri0.getLocalName().trim();
				String ln1 = uri1.getLocalName().trim();
		
				// pre-porcess 
				ln0 = ln0.replaceAll("[0-9]", "");
				ln1 = ln1.replaceAll("[0-9]", "");
				
				System.out.println(ln0);
				System.out.println(ln1);

				if (ln0.length() == 0 || ln1.length() == 0)
					return 0.0;
		
				// return (new StoilosMetric()).getSimilarity(ln1, ln2);
				return (new Levenshtein()).getSimilarity(ln0, ln1);
			}
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace(System.err);
			return 0.0;
		}

	}
}
