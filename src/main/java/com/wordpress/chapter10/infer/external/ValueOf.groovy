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

public class ValueOf implements ExternalFunction {
	

	public ValueOf() {}

	
	
	@Override
	public int getArity() {
		return 1;
	}

	@Override
	public ArgumentType[] getArgumentTypes() {
		return [
			ArgumentType.Double
		].toArray();
	}

	@Override
	public double getValue(ReadOnlyDatabase db, GroundTerm... args) {
		
		
		String arg0 = args[0].toString().trim().replace('\'', "");
		System.out.println("------------*** " + arg0 + " ***------------")
		
		return Double.valueOf(arg0);
	}
	


}
