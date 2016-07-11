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

public class EqualPrefixImpl implements ExternalFunction {

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
		//return args[0].getValue().equals(args[1].getValue()) ? 1.0 : 0.0;
		String arg0 = args[0].toString().trim();
		String arg1 = args[1].toString().trim();

		//System.err.printf("********* %s, %s *********\n", arg0, arg1);

		try {

			// create URI objects from arguments.
			// If any is not a URI then return 0.
			URI uri0 = (new ValueFactoryImpl()).createURI(arg0);
			URI uri1 = (new ValueFactoryImpl()).createURI(arg1);

			// if namespaces are the same, then compute exact similarity
			if (uri0.getNamespace().equals(uri1.getNamespace())) {

				return 1.0;
			} else {
				return 0.0;
			}

		} catch (IllegalArgumentException ex) {
			ex.printStackTrace(System.err);
			return 0.0;
		}
	}

}
