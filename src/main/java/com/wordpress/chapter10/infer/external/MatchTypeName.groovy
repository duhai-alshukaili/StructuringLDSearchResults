/**
 * FileName: RunERSimInfer.groovy
 * Date    : 20-4-2015
 *
 * @author Duhai Alshukaili
 *
 * A groovy class for the ER sim inference rules
 */

package com.wordpress.chapter10.infer.external;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import edu.umd.cs.psl.database.ReadOnlyDatabase;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm
import edu.umd.cs.psl.model.function.ExternalFunction;

public class MatchTypeName implements ExternalFunction {

	@Override
	public int getArity() {
		return 2;
	}

	@Override
	public ArgumentType[] getArgumentTypes() {
		return [
			ArgumentType.UniqueID,
			ArgumentType.String
		].toArray();
	}

	@Override
	public double getValue(ReadOnlyDatabase db, GroundTerm... args) {
		//return args[0].getValue().equals(args[1].getValue()) ? 1.0 : 0.0;
		String arg0 = args[0].toString().trim();
		String arg1 = args[1].getValue();
		
		String ln0 = localName(arg0);
		String ln1 = localName(arg1);
		
		if (ln0.length()==0 || ln1.length()==0) return 0.0;
		
		ln0 = ln0.toLowerCase();
		ln1 = ln1.toLowerCase();
		
		float sim = (new Levenshtein()).getSimilarity(ln0, ln1);
		
		System.out.printf("Matching: %s, %s, %f\n", ln0, ln1, sim);
		
		if (sim >= 0.8) return 1.0; else return 0.0; 
	}
	
	
	private static String localName(String uri) {
		try {
			URI uri0 = (new ValueFactoryImpl()).createURI(uri);
			return uri0.getLocalName();
		} catch (IllegalArgumentException ex) {
			return uri;
		}

	}

}
