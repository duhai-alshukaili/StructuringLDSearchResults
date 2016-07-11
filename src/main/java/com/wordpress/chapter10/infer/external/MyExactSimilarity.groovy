package com.wordpress.chapter10.infer.external;

import edu.umd.cs.psl.database.ReadOnlyDatabase;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm
import edu.umd.cs.psl.model.function.ExternalFunction;

public class MyExactSimilarity implements ExternalFunction {
	
	 @Override
	 public int getArity() {
		 return 2;
	 }

	 @Override
	 public ArgumentType[] getArgumentTypes() {
		 return [ArgumentType.UniqueID, ArgumentType.UniqueID].toArray();
	 }
	
	 @Override
	 public double getValue(ReadOnlyDatabase db, GroundTerm... args) {
		 return args[0].toString().equals(args[1].toString()) ? 1.0 : 0.0;
	 }
	
}
