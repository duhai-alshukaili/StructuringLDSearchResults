/**
 * FileName: RunERSimInfer.groovy
 * Date    : 20-4-2015
 *
 * @author Duhai Alshukaili
 *
 * A groovy class for the ER sim inference rules
 */

package com.wordpress.chapter10.infer.external;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.time.DateUtils;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import uk.ac.man.cs.rdb.LiteralBean;
import uk.ac.man.cs.rdb.LiteralJDBCDAO;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import edu.umd.cs.psl.database.ReadOnlyDatabase;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm
import edu.umd.cs.psl.model.function.ExternalFunction;

public class LexicalDateStringSimilarity implements ExternalFunction {
	
	// the data access object for the literals database
	private static LiteralJDBCDAO literalDAO;
	
	private static HashMap<Integer, LiteralBean> beanMap;
	
	public LexicalDateStringSimilarity() {
		
		// Intialize the data access object
		//literalDAO = new LiteralJDBCDAO();

		//beanMap = new HashMap<Integer, LiteralBean>();
		
	}

	
	
	@Override
	public int getArity() {
		return 2;
	}

	@Override
	public ArgumentType[] getArgumentTypes() {
		return [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID
		].toArray();
	}

	@Override
	public double getValue(ReadOnlyDatabase db, GroundTerm... args) {
		String arg0 = args[0].toString().trim();
		String arg1 = args[1].toString().trim();
		
		/*
		int id0 = Integer.parseInt(arg0.substring(arg0.indexOf("_")+1));
		int id1 = Integer.parseInt(arg1.substring(arg1.indexOf("_")+1));
		
		LiteralBean bean0 = loadBean(id0);
		LiteralBean bean1 = loadBean(id1);
		*/
		
		//return simNumber(parseDateObject(bean0.getLiteralString())
		//			    ,parseDateObject(bean1.getLiteralString()));
					
		//return (new Levenshtein()).getSimilarity(bean0.getLiteralString(), bean1.getLiteralString())
		
		//if (bean0.getLiteralString().equals(bean1.getLiteralString()))
		if (arg0.equals(arg1))
			return 1.0
		else
			return 0.0;
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
	
	/**
	 * 
	 * @param strValue
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
