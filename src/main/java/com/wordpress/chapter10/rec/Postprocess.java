package com.wordpress.chapter10.rec;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import uk.ac.man.cs.stdlib.PairOfStrings;

public class Postprocess {

	public static void main(String[] args) {

		if (args.length < 2) {
			System.out.println("Usage: java Postprocess "
					+ "[Result_File] [GT_File]");

			System.exit(1);
		}

		ArrayList<PairOfStrings> gtList = loadGTFile(args[1]);
		ArrayList<Tuple> resultList = loadResultFile(args[0], gtList);
		
		Collections.sort(resultList, Collections.reverseOrder());
		emit(resultList, args[0]);
	}


	/**
	 * 
	 * @param resultList
	 * @param outPth
	 */
	private static void emit(ArrayList<Tuple> resultList, String outPth) {
		
		try {
			CSVPrinter printer = new CSVPrinter(new FileWriter(outPth),
					CSVFormat.DEFAULT);
			
			for (Tuple t: resultList) {
				printer.printRecord(t.getEntity1(), 
						t.getEntity2(), 
						t.getScore(), t.isSame() ? "1" : "0");
			}
			
			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * 
	 * @param filePath
	 * @param gtList
	 * @return
	 */
	private static ArrayList<Tuple> loadResultFile(String filePath, 
			ArrayList<PairOfStrings> gtList) {
		
		ArrayList<Tuple> resultList = new ArrayList<Tuple>();
		
		try {
			Iterable<CSVRecord> records = CSVFormat.DEFAULT
					.parse(new FileReader(filePath));

			for (CSVRecord record : records) {
				
				String e1 = record.get(0);
			    String e2 = record.get(1);
			    Double score = Double.parseDouble(record.get(2));
			    
			    PairOfStrings p = new PairOfStrings(e1, e2);
			    
			    if (gtList.contains(p)) {
			    	resultList.add(new Tuple(e1, e2, score, true));
			    } else {
			    	resultList.add(new Tuple(e1, e2, score, false));
			    }
				
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resultList;
	}

	/**
	 * 
	 * @param filePath
	 * @return
	 */
	private static ArrayList<PairOfStrings> loadGTFile(String filePath) {

		ArrayList<PairOfStrings> gtList = new ArrayList<PairOfStrings>();

		try {
			Iterable<CSVRecord> records = CSVFormat.DEFAULT
					.parse(new FileReader(filePath));

			for (CSVRecord record : records) {

				if (Double.parseDouble(record.get(2)) == 1.0) {
					gtList.add(new PairOfStrings(record.get(0), record.get(1)));
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return gtList;
	}

}
