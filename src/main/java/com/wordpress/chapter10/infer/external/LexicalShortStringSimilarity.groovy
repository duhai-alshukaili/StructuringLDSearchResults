/**
 * FileName: RunERSimInfer.groovy
 * Date    : 20-4-2015
 *
 * @author Duhai Alshukaili
 *
 * A groovy class for the ER sim inference rules
 */

package com.wordpress.chapter10.infer.external;

import java.util.HashMap;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import uk.ac.man.cs.rdb.LiteralBean;
import uk.ac.man.cs.rdb.LiteralJDBCDAO;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import edu.umd.cs.psl.database.ReadOnlyDatabase;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm
import edu.umd.cs.psl.model.function.ExternalFunction;

public class LexicalShortStringSimilarity implements ExternalFunction {
	
	// the data access object for the literals database
	private static LiteralJDBCDAO literalDAO;
	
	private static HashMap<Integer, LiteralBean> beanMap;
	
	public LexicalShortStringSimilarity() {
		
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
		
		//return (new Levenshtein()).getSimilarity(bean0.getLiteralString(), 
		//	                                     bean1.getLiteralString());
											 
		return (new Levenshtein()).getSimilarity(arg0, arg1);
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

}
