package com.wordpress.chapter10.rec;



import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import uk.ac.man.cs.stdlib.PairOfStrings;

import java.util.HashMap;



public class LoadGroundTruth {
	
	private HashMap<PairOfStrings,Double> gtData;
	
	/**
	 * @return the gtData
	 */
	public HashMap<PairOfStrings, Double> getGtData() {
		return gtData;
	}


	public LoadGroundTruth(String gtFile) {
		
		gtData = new HashMap<PairOfStrings,Double>();
		
		File fXmlFile = new File(gtFile);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			
	
			
			dBuilder = dbFactory.newDocumentBuilder();
		    Document doc = dBuilder.parse(fXmlFile);
		    
		    //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		    doc.getDocumentElement().normalize();
		    
		    
		    //System.out.println("Root element :" + 
		    //		doc.getDocumentElement().getNodeName());
		    
		    NodeList nList = doc.getElementsByTagName("Cell");
		    
		    for (int temp = 0; temp < nList.getLength(); temp++) {
		    	
		    	Node nNode = nList.item(temp);
				
		    	if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		    		Element cElement = (Element) nNode;
		    		
		    		String entity1 = cElement.getElementsByTagName("entity1").item(0).getAttributes().item(0).getTextContent();
		    		String entity2 = cElement.getElementsByTagName("entity2").item(0).getAttributes().item(0).getTextContent();
		    		Double measure = Double.parseDouble(cElement.getElementsByTagName("measure").item(0).getTextContent());
		    		String relation = cElement.getElementsByTagName("relation").item(0).getTextContent();	
		    		
		    		if (relation.trim().equals("=")) {
		    			
		    			gtData.put(new PairOfStrings(entity1,entity2), measure);
		    		} // end if		
		    	} // end if nNode
		    	
		    } // end for temp
		    
		    
		    
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new ExceptionInInitializerError(e);
		}
		
	}
	
	
	
	/*
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println(
					  "Usage: java GenerateGroundTruth "
					+ "[mappings.rdf] [baseURI] [OUT_FOLDER]");
			
			System.exit(1);
		}
		
		
		File mappingsFile = new File(args[0]);
		String baseURI = args[1];
		
		try {
			
			// create a repo
			Repository repo = new SailRepository(new MemoryStore());
			repo.initialize();
			
			RepositoryConnection con = repo.getConnection();
			
			con.add(mappingsFile, baseURI, RDFFormat.RDFXML);
			
			
			RepositoryResult<Statement> statements = 
					con.getStatements(null, null, null, true);
			
			Model mappingsModel = Iterations.addAll(statements, 
					new LinkedHashModel());
			
			for (Statement stmt: mappingsModel) {
				System.out.println(stmt.toString());
			}
		
		} catch (RepositoryException | RDFParseException | IOException e) {
			e.printStackTrace();
		}
	}
	*/

}
