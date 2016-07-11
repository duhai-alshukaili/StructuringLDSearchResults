package com.wordpress.chapter10.rec;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;
import edu.princeton.cs.algs4.SET;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;

public class Sample {
	
	
	private List<String> typeList;
	
	private int sampleSize;
	
	private String queryServiceURI;
	
	private List<String> sample;
	
	

	/**
	 * 
	 * @param queryServiceURI
	 * @param typeList
	 * @param sampleSize
	 */
	public Sample(String queryServiceURI, List<String> typeList, 
			int sampleSize) {
		
		this.sampleSize = sampleSize;
		this.queryServiceURI = queryServiceURI;
		this.typeList = typeList;
		this.sample = new ArrayList<String>();
		
		sample();
		
	}
	
	
	/**
	 * 
	 */
	private void sample() {
		
		String queryString = createQueryString();
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = 
				QueryExecutionFactory.createServiceRequest(
						this.queryServiceURI, query); 

			ResultSet results = qexec.execSelect();

			
			//int count = 0;
			// we don't now the size of this
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				RDFNode s = soln.get("s");
				
				if (sample.size() <= sampleSize) {
					
					if (!sample.contains(s.toString()))
						sample.add(s.toString());
				} else {
					int r = StdRandom.uniform(sample.size());
					if (r < sampleSize)
					{
						if (!sample.contains(s.toString()))
							sample.set(r, s.toString());
					}
						
				}
				
			} // finished iterating over the result set
	}


	private String createQueryString() {
		
		StringBuilder types = new StringBuilder();
		
		types.append("(");
		
		for (int i=0; i < typeList.size()-1;i++) {
			types.append("<").append(typeList.get(i)).append(">, ");
		}
		
		types.append("<").append(typeList.get(typeList.size()-1)).append(">)");
		
		StringBuilder queryString = new StringBuilder();
		
		queryString.append("select distinct ?s")
		           .append(" where {?s  a ?type ;")
		           .append(" <http://www.w3.org/2002/07/owl#sameAs> ?x .")
		           .append(" FILTER (?type IN").append(types.toString())
		           .append(") . }");
		
		
		System.out.println(queryString);
		
		
		return queryString.toString();
	}


	/**
	 * @return the sample
	 */
	public List<String> getSample() {
		return sample;
	}
	
	
	public static void main(String args[]) {
		
		List<String> typeList = new ArrayList<String>();
		typeList.add("http://dbpedia.org/ontology/Film");
		
		String serviceSring = "http://model.cs.man.ac.uk:3030/DBpedia/query";
		
		Sample sample = new Sample(serviceSring,typeList,50);
		
		
		for (String s: sample.getSample()) {
			
			StdOut.println(s);
		}
		
		
	}
	
	

}
