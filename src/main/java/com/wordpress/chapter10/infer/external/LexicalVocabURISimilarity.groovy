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

public class LexicalVocabURISimilarity implements ExternalFunction {

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
		
		if (ln0.length()==0 || ln1.length()==0) return 0.0;
		
		return (new Levenshtein()).getSimilarity(ln0, ln1);

		/*
		try {
			
			// create URI objects from arguments.
			// If any is not a URI then return 0.
			URI uri0 = (new ValueFactoryImpl()).createURI(arg0);
			URI uri1 = (new ValueFactoryImpl()).createURI(arg1);

			// if namespaces are the same, then compute exact similarity
			if (uri0.getNamespace().equals(uri1.getNamespace())) {
				if (uri0.getLocalName().equals(uri1.getLocalName())) {
					return 1.0;
				} else {
					return 0.0;
				}
			} else {
				String ln1 = uri0.getLocalName().trim();
				String ln2 = uri1.getLocalName().trim();
				
				if (ln1.length()==0 || ln2.length()==0) return 0.0;
				
				//return (new StoilosMetric()).getSimilarity(ln1, ln2);
				return (new Levenshtein()).getSimilarity(ln1, ln2);
			}
		} catch (IllegalArgumentException ex) {
			//ex.printStackTrace(System.err);
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
