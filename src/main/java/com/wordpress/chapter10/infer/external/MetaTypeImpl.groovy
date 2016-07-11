package com.wordpress.chapter10.infer.external;

import edu.umd.cs.psl.database.ReadOnlyDatabase;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm;
import edu.umd.cs.psl.model.function.ExternalFunction;

public class MetaTypeImpl  implements ExternalFunction {
	
	
	 ArrayList<String> metaTypeList;
	 
	 public MetaTypeImpl() {
		 metaTypeList = new ArrayList<String>();
		 
		 metaTypeList.add("rdfs:Class");
		 metaTypeList.add("rdf:Property");

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
		 //return args[0].getValue().equals(args[1].getValue()) ? 1.0 : 0.0;
		 
		 String val = args[0].toString();
		 //String val = args[0].getValue();
		 
		 for (String t: metaTypeList) {
			 if (val.equals(t))
			 	return 1.0;
		 }
		 
		 return 0.0;
		 
		 
		 
	 }
	
}