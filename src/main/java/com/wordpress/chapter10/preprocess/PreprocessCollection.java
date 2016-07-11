package com.wordpress.chapter10.preprocess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import resources.data.PrefixesMap;
import uk.ac.man.cs.rdb.LiteralBean;
import uk.ac.man.cs.rdb.LiteralJDBCDAO;
import uk.ac.man.cs.stdlib.PairOfStrings;
import uk.ac.man.cs.string.StoilosMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.CosineSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;
import com.wcohen.ss.SoftTFIDF;

import edu.princeton.cs.algs4.In;

public class PreprocessCollection {

	private static BidiMap<String,String> literalMap = 
			new DualHashBidiMap<String,String>();
	
	private static int litID = 1;
	
	private static String LIT_PREFIX = "::lit_";
	
	
	// data structures for saving pre-processed data
	static Set<String>         entitySet   = new HashSet<String>();
	
	static Set<PairOfStrings>  hasDateLit  = new HashSet<PairOfStrings>();
	static Set<DateLiteral>    dateLiteral = new HashSet<DateLiteral>();
	
	static Set<PairOfStrings>  hasNumLit   = new HashSet<PairOfStrings>();
	static Set<NumericLiteral> numLiteral  = new HashSet<NumericLiteral>();
	
	static Set<PairOfStrings> hasShortStrLit  = new HashSet<PairOfStrings>();
	static Set<StringLiteral> shortStrtLit     = new HashSet<StringLiteral>();
	
	static Set<PairOfStrings> hasLongStrLit  = new HashSet<PairOfStrings>();
	static Set<StringLiteral> longStrtLit     = new HashSet<StringLiteral>();
	
	static Set<PairOfStrings> hasURIVal = new HashSet<PairOfStrings>();
	static Set<String> uris = new HashSet<String>();
	
	public static void main(String[] args)  {

		if (args.length < 2) {
			System.err.println("Usage: PreprocessCollection "
					+ "collection_file.txt output_path");
			System.exit(1);
		}


		PrintUtil.registerPrefixMap(PrefixesMap.INSTANCE.load());

		In collectionIn = new In(args[0]);

		while (collectionIn.hasNextLine()) {
			String rdfFile = collectionIn.readLine();

			if (rdfFile.startsWith("//") || rdfFile.trim().length() == 0)
				continue;

			Model model = ModelFactory.createDefaultModel();

			model.read(rdfFile);

			StmtIterator iter = model.listStatements();

			while (iter.hasNext()) {

				Statement stmt = iter.next();
				


				// apply statements filter
				if (!valid(stmt))
					continue;
				
				String entity = PrintUtil.print(stmt.getSubject());
				entitySet.add(entity);
				
				if (stmt.getObject().isLiteral()) {
					
					if (hasDateObject(stmt)) {
						
						String dateString = stmt.getObject().asLiteral().getLexicalForm();
						
						long dateValue = parseDateObject(dateString);
						
						String id = literalID(dateString);
						
						// create a literal object then add it to a set
						DateLiteral literal = new DateLiteral(id, dateValue, dateString);
						dateLiteral.add(literal);
						
						// entry of HasDateLit predicates
						PairOfStrings p = new PairOfStrings(entity, id);
						hasDateLit.add(p);
						
						
					} else if (hasNumericObject(stmt)) {
						String strValue = stmt.getObject().asLiteral().getLexicalForm();
						
						double value = parseNumricObject(strValue);
						
						String id = literalID(strValue);
						
						// create a literal object then add it to a set
						NumericLiteral literal = new NumericLiteral(id, value, strValue);
						numLiteral.add(literal);
						
						// entry of HasDateLit predicates
						PairOfStrings p = new PairOfStrings(entity, id);
						hasNumLit.add(p);

					} else if (hasShortString(stmt)) {
						String value = stmt.getObject().asLiteral().getLexicalForm();
						String id = literalID(value);
						
						StringLiteral literal = new StringLiteral(id, value);
						shortStrtLit.add(literal);
						
						PairOfStrings p = new PairOfStrings(entity, id);
						hasShortStrLit.add(p);
					} else {
						String value = stmt.getObject().asLiteral().getLexicalForm();
						String id = literalID(value);
						
						StringLiteral literal = new StringLiteral(id, value);
						longStrtLit.add(literal);
						
						PairOfStrings p = new PairOfStrings(entity, id);
						hasLongStrLit.add(p);
					}
				} else {
					String predicate = PrintUtil.print(stmt.getPredicate());
					
					if (!predicate.equals("rdf:type")) {
						String object = stmt.getObject().asResource().toString();
						PairOfStrings p = new PairOfStrings(entity, object);
						hasURIVal.add(p);
						uris.add(object);
					}
				}
			} // end while iter
		} // end while In

		
		// now we compute the lexical similarities
		List<SimData> simLit = new ArrayList<SimData>(); // literal similarities
		List<SimData> simURI = new ArrayList<SimData>(); // URI similarities
		
		System.out.println("[info]: lexical similarities for short literals");
		// lexical similarities for short literals
		for (StringLiteral l1: shortStrtLit) {
			for (StringLiteral l2: shortStrtLit) {
				
				SimData e = new SimData(l1.getID(), l2.getID(), 
						simShortString(l1.getValue(), l2.getValue()));
				simLit.add(e);
			}
		}
		
		System.out.println("[info]: lexical similarities for long literals");
		// lexical similarities for long literals
				for (StringLiteral l1: longStrtLit) {
					for (StringLiteral l2: longStrtLit) {
						
						SimData e = new SimData(l1.getID(), l2.getID(), 
								simLongString(l1.getValue(), l2.getValue()));
						simLit.add(e);
					}
				}
		
		// lexical similarities for dates
		System.out.println("[info]: lexical similarities for dates");
		for (DateLiteral l1: dateLiteral) {
			for (DateLiteral l2: dateLiteral) {
				SimData e = new SimData(l1.getID(), l2.getID(),
						simShortString(l1.getDateString(), l2.getDateString()));
				simLit.add(e);
				System.err.println(new SimData(l1.getDateString(), l2.getDateString(), 
						simNumber(l1.getValue(), l2.getValue())));
			}
		}
		
		// lexical similarities for numbers
		System.out.println("[info]: lexical similarities for numbers");
		for (NumericLiteral l1: numLiteral) {
			for (NumericLiteral l2: numLiteral) {
				SimData e = new SimData(l1.getID(), l2.getID(),
						simNumber(l1.getValue(), l2.getValue()));
				simLit.add(e);
			}
		}
		
		// lexical similarities for uris
		System.out.println("[info]: lexical similarities for uris");
		for (String s1: uris) {
			for (String s2: uris) {
				SimData e = new SimData(s1, s2, simURI(s1, s2));
				simURI.add(e);
			}
		}
		
		// sort similarities
		Collections.sort(simLit);
		Collections.sort(simURI);
		
		// now we print the data to the output folder
		String dataroot = args[1];
		
		System.out.println("[info]: genertating files");
		printSimilarities(dataroot, simLit, "SimLit.csv");	
		printSimilarities(dataroot, simURI, "SimURI.csv");
		printPairSet(dataroot, hasShortStrLit, "HasShortStrLit.csv");
		printPairSet(dataroot, hasLongStrLit,  "HasLongStrLit.csv");
		printPairSet(dataroot, hasDateLit, 	   "HasDatetLit.csv");
		printPairSet(dataroot, hasNumLit,      "HasNumLit.csv");
		printPairSet(dataroot, hasURIVal,      "HasURIVal.csv");
		printBiMap(dataroot, literalMap, "litDB.csv");
		printSet(dataroot, entitySet, "Entity.csv");

		
	}
	
	private static void printSet(String dataroot, Set<String> set,
			String fileName) {
		CSVPrinter printer;
		try {
			printer = new CSVPrinter(new FileWriter(dataroot
					+ "/" + fileName), CSVFormat.DEFAULT);
			
			for (String e : set) {
				printer.printRecord(e);
			}

			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void printBiMap(String dataroot,
			BidiMap<String, String> map, String fileName) {
		CSVPrinter printer;
		try {
			printer = new CSVPrinter(new FileWriter(dataroot
					+ "/" + fileName), CSVFormat.DEFAULT);
			
			for (String id : map.keySet()) {
				printer.printRecord(id, map.get(id));
			}

			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void printPairSet(String dataroot,
			Set<PairOfStrings> pairSet, String fileName) {
		
		CSVPrinter printer;
		try {
			printer = new CSVPrinter(new FileWriter(dataroot
					+ "/" + fileName), CSVFormat.DEFAULT);
			for (PairOfStrings p : pairSet) {
				printer.printRecord(p.getFirst(), p.getSecond());
			}

			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void printSimilarities(String dataroot
			, List<SimData> simList
			, String fileName) {
		CSVPrinter printer;
		try {
			printer = new CSVPrinter(new FileWriter(dataroot
					+ "/" + fileName), CSVFormat.DEFAULT);
			for (SimData data : simList) {
				printer.printRecord(data.firstEntry, data.secondEntry,
						data.similarity);
			}

			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * 
	 * @param dateString
	 * @return
	 */
	private static String literalID(String value) {
		if (literalMap.containsValue(value)) {
			return literalMap.getKey(value);
		} else {
			String id = LIT_PREFIX + litID;
			literalMap.put(id, value);
			litID++;
			return id;
		}
		
	}


	/**
	 * 
	 * @param stmt
	 * @return
	 */
	private static boolean hasShortString(Statement stmt) {
		String value = stmt.getLiteral().getLexicalForm();
		
		return value.trim().length() <= 20;
	}


	/**
	 * 
	 * @param strValue
	 * @return
	 */
	private static double parseNumricObject(String strValue) {
		
		return NumberUtils.createDouble(strValue);
	}


	/**
	 * Try to detect if the object value of given statement is a numeric.
	 * @param stmt
	 * @return
	 */
	private static boolean hasNumericObject(Statement stmt) {
		
		String value = stmt.getLiteral().getLexicalForm();
		
		if (isNumeric(value))
			return true;
		else
			return false;
	}


	/**
	 * 
	 * @param object
	 * @return
	 */
	private static long parseDateObject(String strValue) {
		
		try {
			Date d = DateUtils.parseDate(strValue, 
						"yyyy",
						"dd-MM-yy",
						"dd-MM-yyZZ",
						"dd-MM-yyyy",
						"dd-MM-yyyyZZ",
						"dd/MM/yy",
						"dd/MM/yyZZ",
						"yyyy-MM-dd",
						"yyyy-MM-dd'T'HH:mm:ss",
						"yyyy-MM-dd'T'HH:mm:ssZZ",
						"yyyy-MM-ddZZ",
						"'T'HH:mm:ss", 
						"'T'HH:mm:ssZZ", 
						"HH:mm:ss", 
						"HH:mm:ssZZ",
						"EEE, dd MMM yyyy HH:mm:ss Z",
						"EEE, MMM d, yy");
			
			return d.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
			return 0;
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
	private static boolean hasDateObject(Statement stmt) {
		
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
	 * @param str
	 * @return
	 */
	private static boolean isNumeric(String str) {

		return NumberUtils.isNumber(str);
		
		// try {
		// double d = Double.parseDouble(str);
		// } catch (NumberFormatException nfe) {
		// return false;
		// }
		// return true;
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

	
	

	

	private static boolean valid(Statement stmt) {

		if (stmt.getSubject().isAnon() || stmt.getObject().isAnon())
			return false;

		if (stmt.getPredicate().toString()
				.startsWith("http://dbpedia.org/property/"))
			return false;

		if (stmt.getObject().isLiteral()) {

			String lang = stmt.getLanguage();

			if (!lang.equals("") && !lang.equals("en"))
				return false;

		}

		return true;
	}
	
	/**
	 * 
	 * @return
	 */
	private static double simShortString(String s1, String s2) {
		
		Levenshtein m1 = new Levenshtein();
		JaroWinkler m2 = new JaroWinkler();
		QGramsDistance m3 = new QGramsDistance();

		return (  m1.getSimilarity(s1, s2) 
				+ m2.getSimilarity(s1, s2)
				+ m3.getSimilarity(s1, s2)) / 3.0;
	}
	
	/**
	 * 
	 * @return
	 */
	private static double simLongString(String s1, String s2) {
		
		//Levenshtein m1 = new Levenshtein();
		CosineSimilarity m2 = new CosineSimilarity();
		//SoftTFIDF m1 = new SoftTFIDF();
		//m1.score(s1, s2)

		return (m2.getSimilarity(s1, s2)) / 1.0;
	}
	
	/**
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private static double simURI(String s1, String s2) {
		
		try {
			
			// create URI objects from arguments.
			// If any is not a URI then return 0.
			URI u1 = (new ValueFactoryImpl()).createURI(s1);
			URI u2 = (new ValueFactoryImpl()).createURI(s2);
			
			if (u1.getNamespace().equals(u2.getNamespace())) {
				if (u1.getLocalName().equals(u2.getLocalName())) {
					return 1.0;
				} else {
					return 0.0;
				}
			} else {
				String ln1 = u1.getLocalName().trim();
				String ln2 = u2.getLocalName().trim();
		
				// pre-porcess 
				ln1 = ln1.replaceAll("[0-9]", "");
				ln2 = ln2.replaceAll("[0-9]", "");

				if (ln1.length() <= 1 || ln2.length() <= 1)
					return 0.0;
		
				 //return (new StoilosMetric()).getSimilarity(ln1, ln2);
				//return (new Levenshtein()).getSimilarity(ln1, ln2);
				 //return (new QGramsDistance()).getSimilarity(ln1, ln2);
				return simShortString(ln1, ln2);
			}

		
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace(System.err);
			return 0.0;
		}
		
					
	}


	/**
	 * 
	 * @param uri
	 * @return
	 */
	private static boolean hasLocalName(URI uri) {
		String ln = uri.getLocalName().trim();
		
		if (ln == null || ln.length() == 0) 
			return true;
		else
			return false;
	}
	
	/**
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	private static double simNumber(double d1, double d2) {
		
		if (!(d1 == 0 && d2 == 0)) {
			return (1 - ((Math.abs(d1 - d2)) / (Math.abs(d1) + Math.abs(d2))));
		} else {
			return 1.0;
		}
	}
	
	/**
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	private static double simNumber(long d1, long d2) {
		
		if (!(d1 == 0 && d2 == 0)) {
			return (1.0 - ((double)(Math.abs(d1 - d2)) / (Math.abs(d1) + Math.abs(d2))));
		} else {
			return 1.0;
		}
	}
	
	

}