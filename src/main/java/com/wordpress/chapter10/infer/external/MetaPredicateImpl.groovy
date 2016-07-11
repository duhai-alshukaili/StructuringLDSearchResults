package com.wordpress.chapter10.infer.external;

import edu.umd.cs.psl.database.ReadOnlyDatabase;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm;
import edu.umd.cs.psl.model.function.ExternalFunction;

public class MetaPredicateImpl  implements ExternalFunction {
	
	
	 ArrayList<String> metaPredList;
	 
	 public MetaPredicateImpl() {
		 metaPredList = new ArrayList<String>();
		 
		 metaPredList.add("rdfs:range");
		 metaPredList.add("rdfs:domain");
		 metaPredList.add("rdfs:subClassOf");
		 metaPredList.add("rdfs:subPropertyOf");
		 metaPredList.add("rdf:type");
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
		 
		 for (String p: metaPredList) {
			 if (val.equals(p))
				 return 1.0;
		 }
		 
		 return 0.0;
	 }
	
}