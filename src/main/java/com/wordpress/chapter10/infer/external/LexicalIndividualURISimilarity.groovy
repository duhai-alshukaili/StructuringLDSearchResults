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

public class LexicalIndividualURISimilarity implements ExternalFunction {

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
		
		
		String ln0 = localName(arg0);
		String ln1 = localName(arg1);
		
		if (ln0.equals(ln1)) {
			return 1.0;
		}

		String cleanL0 = ln0.replaceAll("[0-9]", "");
		String cleanL1 = ln1.replaceAll("[0-9]", "");
		
		// throw away stuff that has dominating digits
		if ( (ln0.length() - cleanL0.length()) >= cleanL0.length() )
			return 0.0;
			
		if ( (ln1.length() - cleanL1.length()) >= cleanL1.length() )
			return 0.0;
			
		return (new Levenshtein()).getSimilarity(cleanL0, cleanL1);
		
		/*
		try {
			
			// create URI objects from arguments.
			// If any is not a URI then return 0.
			URI uri0 = (new ValueFactoryImpl()).createURI(arg0);
			URI uri1 = (new ValueFactoryImpl()).createURI(arg1);
			
			String ln0 = uri0.getLocalName();
			String ln1 = uri1.getLocalName();
			
			if (ln0.equals(ln1)) {
				return 1.0;
			}

			String cleanL0 = ln0.replaceAll("[0-9]", "");
			String cleanL1 = ln1.replaceAll("[0-9]", "");
			
			// throw away stuff that has dominating digits
			if ( (ln0.length() - cleanL0.length()) >= cleanL0.length() )
				return 0.0;
				
			if ( (ln1.length() - cleanL1.length()) >= cleanL1.length() )
				return 0.0;
				
			return (new Levenshtein()).getSimilarity(cleanL0, cleanL1);
				
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace(System.err);
			return 0.0;
		}
		*/
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
