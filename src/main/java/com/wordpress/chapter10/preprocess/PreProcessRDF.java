package com.wordpress.chapter10.preprocess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

import resources.data.PrefixesMap;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;

import edu.princeton.cs.algs4.In;
import uk.ac.man.cs.rdb.LiteralBean;
import uk.ac.man.cs.rdb.LiteralJDBCDAO;
import uk.ac.man.cs.stdlib.Triple;

public class PreProcessRDF {

	// the data access object for the literals database
	//private static LiteralJDBCDAO literalDAO;

	// Literal prefix
	//private static String LIT_PREFIX = "::Lit_";

	//private static HashMap<Integer, LiteralBean> beanMap;

	public static void main(String args[]) {

		if (args.length < 1) {
			System.err.println("Usage: PreProcessRDF "
					+ "Collection_dir");
			System.exit(1);
		}

		// load prefix map
		PrintUtil.registerPrefixMap(PrefixesMap.INSTANCE.load());

		// intialize the data access object
		//literalDAO = new LiteralJDBCDAO();

		// create a bean map
		//beanMap = new HashMap<Integer, LiteralBean>();
		
		// store all the triples in here 
		// this is loaded during the post processing step
		// no modification of literal values done in this step
		ArrayList<Triple> tripleList = new ArrayList<Triple>();

		// store triple1 pattern (uri,uri,uri)
		ArrayList<Triple> triple1 = new ArrayList<Triple>();

		// store triple2 pattern (uri,uri,literal)
		ArrayList<Triple> triple2 = new ArrayList<Triple>();
		
		// store the id for short string literals
		Set<String> shortStringLit = new HashSet<String>();
		
		Set<String> longStringLit = new HashSet<String>();
		
		Set<String> numericLit = new HashSet<String>();
		
		Set<String> dateLit = new HashSet<String>();
		
		Set<String> typeSet = new HashSet<String>();
		
		Set<String> subjectSet = new HashSet<String>();
		
		Set<String> predicateSet = new HashSet<String>();
		
		Set<String> objectSet = new HashSet<String>();
		

		String dataroot = args[0];
		
		String outPath = dataroot + "/rdf";

		createDir(outPath);

		//
		In inputFile = new In(dataroot + "/collection.txt");

		while (inputFile.hasNextLine()) {

			String rdfFile = inputFile.readLine();

			if (rdfFile.startsWith("//") || rdfFile.trim().length() == 0)
				continue;

			Model model = ModelFactory.createDefaultModel();

			model.read("file:" + rdfFile, "N-TRIPLE");

			StmtIterator iter = model.listStatements();

			while (iter.hasNext()) {

				Statement stmt = iter.next();

				// apply statements filter
				if (!valid(stmt))
					continue;

				Resource subject = stmt.getSubject();
				Property predicate = stmt.getPredicate();
				RDFNode object = stmt.getObject();

				Triple triple = null;
				//Triple t = null;

				if (object.isLiteral()) {

					String lang = stmt.getLanguage();

					String value = object.asLiteral().getLexicalForm().trim();

					if (value.length() > 0) {
						triple = generateTriple(subject, predicate, object); 
						//t = generateTriple(subject, predicate, object); 
					}

					if (triple != null) {

						// add the literal to the literal database if not added
						//LiteralBean bean = literalDAO.add(triple.getObject());

						//String litID = LIT_PREFIX + bean.getId();
						// generate an id
						//triple.setObject(litID);
						
						String lit = triple.getObject();
						
						if (isDateObject(stmt)) {
							// dateLit.add(litID);
							dateLit.add(lit);
						} else if (isNumericObject(stmt)) {
							// numericLit.add(litID);
							numericLit.add(lit);
						} else if (isShortString(stmt)) {
							// shortStringLit.add(litID);
							shortStringLit.add(lit);
						} else if (isLongString(stmt)){
							//longStringLit.add(litID);
							
							if (lit.length() > 255) {
								lit = lit.substring(0, 200);
								//System.out.println(lit);
								longStringLit.add(lit);
								 // System.out.println("---" + lit.length());
								triple.setObject(lit);
							} else {
								longStringLit.add(lit);
								// System.out.println(lit.length());
							}
						}

						if (!triple2.contains(triple))
							triple2.add(triple);
						
						if (!tripleList.contains(triple))
							tripleList.add(triple);
						
						objectSet.add(lit);
						subjectSet.add(triple.getSubject());
						predicateSet.add(triple.getPredicate());
						
					}

				} else {

					triple = generateTriple(subject, predicate, object);
					//t =  generateTriple(subject, predicate, object);

					if (triple != null && !triple1.contains(triple))
						triple1.add(triple);
					
					if (triple != null && !tripleList.contains(triple))
						tripleList.add(triple);
					
					objectSet.add(triple.getObject());
					subjectSet.add(triple.getSubject());
					predicateSet.add(triple.getPredicate());
					
					if (isTypeTriple(stmt)) {
						typeSet.add(triple.getObject());
					}

				} // end if-else literal

			} // end while iter

		} // end while inputFile

		printTripleList(tripleList, outPath + "/triple.csv");
		printTripleList(triple1, outPath + "/triple1.csv");
		printTripleList(triple2, outPath + "/triple2.csv");
		printSet(shortStringLit, outPath + "/shortStringLit.csv");
		printSet(longStringLit, outPath + "/longStringLit.csv");
		printSet(dateLit, outPath + "/dateLit.csv");
		printSet(numericLit, outPath + "/numericLit.csv");
		
		printSet(typeSet, outPath + "/rdftype.csv");
		printSet(subjectSet, outPath + "/subject.csv");
		printSet(predicateSet, outPath + "/predicate.csv");
		printSet(objectSet, outPath + "/object.csv");

	}
	

	/**
	 * 
	 * @param litSet
	 * @param filepath
	 */
	private static void printSet(Set<String> litSet, String filepath) {
		CSVPrinter printer;
		try {
			printer = new CSVPrinter(new FileWriter(filepath),
					CSVFormat.DEFAULT);
			
			for (String s : litSet) {
				printer.printRecord(s);
			}

			printer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 
	 * @param stmt
	 * @return
	 */
	private static boolean isShortString(Statement stmt) {
		String value = stmt.getLiteral().getLexicalForm();
		
		return value.trim().length() <= 25;
	}
	
	/**
	 * 
	 * @param stmt
	 * @return
	 */
	private static boolean isLongString(Statement stmt) {
		String value = stmt.getLiteral().getLexicalForm();
		
		return value.trim().length() >= 50;
	}
	
	/**
	 * Try to detect if the object value of given statement is a numeric.
	 * @param stmt
	 * @return
	 */
	private static boolean isNumericObject(Statement stmt) {
		
		String value = stmt.getLiteral().getLexicalForm();
		
		if (isNumeric(value))
			return true;
		else
			return false;
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	private static boolean isNumeric(String str) {
		return NumberUtils.isNumber(str);
	}
	
	private static boolean isDateTime(String str) {

		try {
			DateUtils.parseDate(str, 
						"dd-MM-yy",
						"dd-MM-yyZZ",
						"dd-MM-yyyy",
						"dd-MM-yyyyZZ",
						"dd/MM/yy",
						"dd/MM/yyZZ",
						"yyyy-MM-dd", 
						"yyyy-MM-ddZZ",
						"'T'HH:mm:ss", 
						"'T'HH:mm:ssZZ", 
						"HH:mm:ss", 
						"HH:mm:ssZZ",
						"EEE, dd MMM yyyy HH:mm:ss Z",
						"EEE, MMM d, yy");
			return true;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Try to detect if the object value of given statement is a date
	 * by checking the defined XML data type, using date hints 
	 * in the property labels and looking for common date formats.
	 * 
	 * @param stmt - an RDF statement with a literal object
	 * @return
	 */
	private static boolean isDateObject(Statement stmt) {
		
		String objectValue = stmt.getObject().asLiteral().getLexicalForm();
		String propertyLabel = stmt.getPredicate().getLocalName();
		RDFDatatype dataType = stmt.getObject().asLiteral().getDatatype();
		
		
		if (dataType != null && isDateType(dataType)) {
			return true;
		} else if (isDateTime(objectValue)) {
			return true;
		} else if (containsDateHint(propertyLabel)) {
			return true;
		}
		
		return false;
	}

	/**
	 * Check if the RDF data type is a date type by looking into the string
	 * of the URL.
	 * 
	 * @param dataType
	 * @return
	 */
	private static boolean isDateType(RDFDatatype dataType) {
		
		String uriString = dataType.getURI();
		
		if (  uriString.endsWith("#date")
		   || uriString.endsWith("#dateTime")
		   || uriString.endsWith("#gDay")
		   || uriString.endsWith("gMonth")
		   || uriString.endsWith("gMonthDay")
		   || uriString.endsWith("gYear")
		   || uriString.endsWith("gYearMonth")
		   || uriString.endsWith("time"))
	
			return true;
		else
			return false;
	}

	/**
	 * Check if the property label contains a hint for a date in its value.
	 * @param propertyLabel
	 * @return
	 */
	private static boolean containsDateHint(String propertyLabel) {
		
		propertyLabel = propertyLabel.toLowerCase();
		if (propertyLabel.contains("date") 
		 || propertyLabel.contains("year")
		 || propertyLabel.contains("month")
		 || propertyLabel.contains("day"))
			return true;
		else
			return false;
	}



	/**
	 * 
	 * @param tripleList
	 * @param path
	 */
	private static void printTripleList(ArrayList<Triple> tripleList,
			String path) {

		CSVPrinter printer;
		try {
			printer = new CSVPrinter(new FileWriter(path),
					CSVFormat.DEFAULT);
			
			for (Triple t : tripleList) {
				printer.printRecord(t.getSubject(), t.getPredicate(), t.getObject());
			}

			printer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @return
	 */
	private static Triple generateTriple(Resource subject, Property predicate,
			RDFNode object) {

		String subString = PrintUtil.print(subject);
		String predString = PrintUtil.print(predicate);
		String objString;

		if (object.isResource()) {
			objString = PrintUtil.print(object);
		} else {

			// get the literal lexical form
			objString = object.asLiteral().getLexicalForm();

		}

		Triple t = new Triple(subString, predString, objString);

		return t;
		// return String.format("\"%s\",\"%s\",\"%s\"", subString, predString,
		// objString);

	}

	/**
	 * 
	 * @param outPath
	 */
	private static void createDir(String dataroot) {

		File outDir = new File(dataroot);

		if (!outDir.exists() || !outDir.isDirectory()) {

			// attempt to create folder
			boolean success = (new File(dataroot)).mkdirs();

			if (!success) {
				System.err.println("[info]: Could not "
						+ "create output folder '" + dataroot + ".");

				System.exit(1);
			}

		} else {

			System.err.println("[info]:  output folder '" + dataroot
					+ " already exists.");
		}

	}

	private static boolean valid(Statement stmt) {

		/*
		if (stmt.getSubject().isAnon() || stmt.getObject().isAnon())
			return false;

		if (stmt.getPredicate().toString()
				.startsWith("http://dbpedia.org/property/"))
			return false;

		if (stmt.getPredicate().toString()
				.equals("http://www.w3.org/2002/07/owl#sameAs")
				&& stmt.getSubject().toString().contains("dbpedia.org/")
				&& stmt.getObject().toString().contains("dbpedia.org"))
			return false;

		if (stmt.getObject().isLiteral()) {
			String lang = stmt.getLanguage();

			if (!lang.equals("") && !lang.equals("en"))
				return false;
		}

		return true;
		*/
		
		if (stmt.getSubject().isAnon() || stmt.getObject().isAnon())
			return false;

		if (stmt.getPredicate().toString()
				.startsWith("http://dbpedia.org/property/"))
			return false;
		// filter the yagos
		if (
		     stmt.getPredicate().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") 
		     && 
			 (
			  stmt.getObject().toString().startsWith("http://dbpedia.org/class/yago/") 
					 || 
			  stmt.getObject().toString().startsWith("http://dbpedia.org/class/yago/")
			 )
		   )
			return false;

		if (stmt.getPredicate().toString()
				.equals("http://www.w3.org/2002/07/owl#sameAs")
				&& stmt.getSubject().toString().contains("dbpedia.org/")
				&& stmt.getObject().toString().contains("dbpedia.org"))
			return false;

		if (stmt.getObject().isLiteral()) {
			String lang = stmt.getLanguage();

			if (!lang.equals("") && !lang.equals("en"))
				return false;
		}

		return true;
	}
	
	
	private static boolean isTypeTriple(Statement stmt) {
		
		if (stmt.getSubject().isAnon() || stmt.getObject().isAnon())
			return false;
		
		if(stmt.getPredicate().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
			return true;
		
		return false;
		
	}

}
