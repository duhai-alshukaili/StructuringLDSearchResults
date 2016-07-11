package com.wordpress.chapter10.infer.external;

import edu.umd.cs.psl.database.ReadOnlyDatabase;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm;
import edu.umd.cs.psl.model.function.ExternalFunction;

public class MetaVocabImpl  implements ExternalFunction {
	
	
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
		 //return args[0].getValue().equals(args[1].getValue()) ? 1.0 : 0.0;
		 
		 String val = args[0].toString();
		 
		 
		 if (val.startsWith("rdf:") || val.startsWith("rdfs:") ||
			 val.startsWith("owl:") || val.startsWith("skos:"))
		 	return 1.0;
		 
		 else
		 	return 0.0;
		 
		 
		 
	 }
	
}