package com.wordpress.chapter10.preprocess;

//import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;








import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.CosineSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;

import resources.data.PrefixesMap;
import uk.ac.man.cs.rdb.LiteralBean;
import uk.ac.man.cs.rdb.LiteralJDBCDAO;
import edu.princeton.cs.algs4.In;
import uk.ac.man.cs.stdlib.Triple;

//import uk.ac.man.cs.sw.SemanticWebTools;

public class PreprocessTriples2 {

	// the data access object for the literals database
	private static LiteralJDBCDAO literalDAO;

	// literl prefix
	private static String LIT_PREFIX = "::Lit_";

	private static HashMap<Integer, LiteralBean> beanMap;

	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			System.err
					.println("Usage: Preprocess collection_file.txt output_path");
			System.exit(1);
		}

		PrintUtil.registerPrefixMap(PrefixesMap.INSTANCE.load());

		// Intialize the data access object
		literalDAO = new LiteralJDBCDAO();

		beanMap = new HashMap<Integer, LiteralBean>();

		String dataroot = args[1];

		File outDir = new File(dataroot);

		if (!outDir.exists() || !outDir.isDirectory()) {

			// attempt to create folder
			boolean success = (new File(dataroot)).mkdirs();
			if (!success) {
				System.err.println("[info]: Could not create output folder '"
						+ dataroot + ".");
				System.exit(1);
			}
		} else {
			System.err.println("[info]:  output folder '" + dataroot
					+ " already exists.");
		}

		In collectionIn = new In(args[0]);

		// store local names for predicates and types
		// HashMap<String, String> localNameMap = new HashMap<String, String>();

		// store triple1 pattern (uri,uri,uri)
		// ArrayList<Triple> list1 = new ArrayList<Triple>();

		// store triple2 pattern (uri,uri,literal)
		ArrayList<Triple> list2 = new ArrayList<Triple>();

		ArrayList<Integer> literalIDs = new ArrayList<Integer>();

		while (collectionIn.hasNextLine()) {
			String rdfFile = collectionIn.readLine();

			if (rdfFile.startsWith("//") || rdfFile.trim().length() == 0)
				continue;

			Model model = ModelFactory.createDefaultModel();

			model.read(rdfFile);

			// Resource centralResource =
			// SemanticWebTools.centralResource(model);

			// StmtIterator iter = model.listStatements(centralResource, null,
			// (RDFNode) null);

			StmtIterator iter = model.listStatements();

			while (iter.hasNext()) {

				Statement stmt = iter.next();

				// apply statements filter
				if (!valid(stmt))
					continue;

				Resource subject = stmt.getSubject();
				Property predicate = stmt.getPredicate();
				RDFNode object = stmt.getObject();

				// localNameMap.put(PrintUtil.print(predicate),
				// predicate.getLocalName());

				// check if this is a type predicate
				// if (predicate.getURI().equals(
				// "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
				// || predicate.getURI().equals(
				// "http://rdf.freebase.com/ns/type.object.type")) {
				// localNameMap.put(PrintUtil.print(object), object.asResource()
				// .getLocalName());
				// }

				Triple triple = null;
				if (object.isLiteral()) {

					String lang = stmt.getLanguage();
					String value = object.asLiteral().getLexicalForm().trim();

					if (value.length() > 0)
						triple = generateTriple(subject, predicate, object);

					if (triple != null) {

						// add the literal to the literal database if not added
						LiteralBean bean = literalDAO.add(triple.getObject());

						// generate an id
						triple.setObject(LIT_PREFIX + bean.getId());

						if (!list2.contains(triple))
							list2.add(triple);

						if (!literalIDs.contains(bean.getId())) {
							literalIDs.add(bean.getId());
						}
					}

				}
			} // end while iter
		} // end while In

		// write triple2 data
		CSVPrinter printer = new CSVPrinter(new FileWriter(dataroot
				+ "/triple2.csv"), CSVFormat.DEFAULT);

		for (Triple t : list2) {
			printer.printRecord(t.getSubject(), t.getPredicate(), t.getObject());
		}

		printer.close();

		// write local name data
		// printer = new CSVPrinter(new FileWriter(dataroot + "/localname.csv"),
		// CSVFormat.DEFAULT);

		// for (String uri : localNameMap.keySet()) {
		// printer.printRecord(uri, localNameMap.get(uri));
		// }

		// printer.close();

		ArrayList<SimData> recordList = new ArrayList<SimData>();
		for (Integer id1 : literalIDs) {

			LiteralBean bean1 = loadBean(id1);

			for (Integer id2 : literalIDs) {

				LiteralBean bean2 = loadBean(id2);

				SimData entry = new SimData(LIT_PREFIX + bean1.getId(),
						LIT_PREFIX + bean2.getId(), similarity(
								bean1.getLiteralString(),
								bean2.getLiteralString()));

				recordList.add(entry);

			} // end for

		} // end for

		// sort similarity values
		Collections.sort(recordList);

		// print the recordList
		// write triple1 data
		printer = new CSVPrinter(new FileWriter(dataroot
								+ "/SimLit.csv"), CSVFormat.DEFAULT);
		
		for (SimData rec: recordList) {
			if (rec.getSimilarity() > 0)
				printer.printRecord(rec.getFirstEntry(), rec.getSecondEntry(), rec.getSimilarity());
		}
		
		printer.close();

	}

	/**
	 * 
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	private static double similarity(String arg0, String arg1) {
		
		
		LiteralType t0 = literalType(arg0);
		LiteralType t1 = literalType(arg1);
		
		if (t0 == t1 && t0 != LiteralType.STR) {

			if (arg0.equals(arg1))
				return 1.0;
			else
				return 0.0;

		} else if (t0 == t1 && t0 == LiteralType.STR){
			AbstractStringMetric metric;

			if (arg0.length() <= 20 && arg1.length() <= 20)
				metric = new Levenshtein();
			else
				metric = new JaccardSimilarity();

			return metric.getSimilarity(arg0, arg1);
		} else {
			return 0.0;
		}
	}

	private static LiteralType literalType(String str) {
		
		if (isDateTime(str)) {
			//System.out.println("*************** " + str + "*************** ");
			return LiteralType.DATE_TIME;
			
		}
		else if (isNumeric(str))
			return LiteralType.NUMBER;
		else
			return LiteralType.STR;
		
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	private  static boolean isNumeric(String str) {
		
		return NumberUtils.isNumber(str);
		//try {
		//	double d = Double.parseDouble(str);
		//} catch (NumberFormatException nfe) {
		//	return false;
		//}
		//return true;
	}
	
	private static boolean isDateTime(String str) {
		
		
		try {
			DateUtils.parseDate(str, "yyyy-MM-dd", "yyyy-MM-ddZZ",
					                  "'T'HH:mm:ss", "'T'HH:mm:ssZZ", 
					                  "HH:mm:ss", "HH:mm:ssZZ");
			return true;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return false;
		}
	}
	
	

	/**
	 * 
	 * @param id
	 * @return
	 */
	private static LiteralBean loadBean(Integer id) {
		if (beanMap.containsKey(id))
			return beanMap.get(id);
		else {
			LiteralBean bean = literalDAO.get(id);
			beanMap.put(id, bean);
			return bean;
		}
	}

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

	}

	private static boolean valid(Statement stmt) {
		
		if (stmt.getSubject().isAnon() || stmt.getObject().isAnon())
			return false;
		
		if (stmt.getPredicate().toString().startsWith("http://dbpedia.org/property/"))
			return false;

		if (!stmt.getObject().isLiteral()) {
			return false;
		} else {

			String lang = stmt.getLanguage();

			if (!lang.equals("") && !lang.equals("en"))
				return false;

		}

		return true;
	}

}
