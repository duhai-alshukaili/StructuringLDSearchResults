package com.wordpress.chapter10.infer.external;

import edu.umd.cs.psl.database.ReadOnlyDatabase;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm;
import edu.umd.cs.psl.model.function.ExternalFunction;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.URI;


public class IsLabelImpl  implements ExternalFunction {



	public IsLabelImpl() {
	}

	@Override
	public int getArity() {
		return 1;
	}

	@Override
	public ArgumentType[] getArgumentTypes() {
		return [ArgumentType.UniqueID].toArray();
	}

	@Override
	public double getValue(ReadOnlyDatabase db, GroundTerm... args) {

		String arg0 = args[0].toString().trim();

		try {

			// create URI objects from arguments.
			// If any is not a URI then return 0.
			URI uri0 = (new ValueFactoryImpl()).createURI(arg0);

			String ln0 = uri0.getLocalName();

			if (ln0.toLowerCase().equals("label"))
				return 1.0;
			else
				return 0.0;



		} catch (IllegalArgumentException ex) {
			ex.printStackTrace(System.err);
			return 0.0;
		}



	}

}