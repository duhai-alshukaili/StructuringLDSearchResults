package com.wordpress.chapter10.infer.external;

import edu.umd.cs.psl.database.ReadOnlyDatabase;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm;
import edu.umd.cs.psl.model.function.ExternalFunction;

public class YagoTypeImpl  implements ExternalFunction {
	
	
	 ArrayList<String> yagoTypeList;
	 
	 public YagoTypeImpl() {
		 yagoTypeList = new ArrayList<String>();
		                   
		 yagoTypeList.add("dbyago:");
		 yagoTypeList.add("yago:");

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
		 
		 for (String t: yagoTypeList) {
			 
			 if (val.startsWith(t)) {
				//System.out.println("[Custom Debug (1)]: " + val); 
			 	return 1.0;
			 }
		 }
		 
		 return 0.0;
		 
	 }
	
}