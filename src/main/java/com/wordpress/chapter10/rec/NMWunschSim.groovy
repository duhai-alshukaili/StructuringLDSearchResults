package com.wordpress.chapter10.rec

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import edu.umd.cs.psl.database.ReadOnlyDatabase;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm;
import edu.umd.cs.psl.model.function.ExternalFunction;

import uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch

class NMWunschSim implements ExternalFunction {

	@Override
	public double getValue(ReadOnlyDatabase db, GroundTerm... args) {
		
		String arg0 = args[0].getValue().trim();
		String arg1 = args[1].getValue().trim();
		
		arg0 = getStringRepresentation(arg0);
		arg1 = getStringRepresentation(arg1);
		
		
		return (new NeedlemanWunch()).getSimilarity(arg0, arg1);
	}
	
	/**
	 * 
	 * @param arg
	 * @return
	 */
	private String getStringRepresentation(String arg) {
		
		try {
			// try to convert to a uri
			URI uri0 = (new ValueFactoryImpl()).createURI(arg);
			
			return normalize(uri0.getLocalName());
		} catch (IllegalArgumentException ex) {
			
			// if fail to convert to a URI return the actual input
			return normalize(arg);
		}	
	}
	
	/**
	 * 
	 * @param arg
	 * @return
	 */
	private String normalize(String arg) {
		
		StringBuilder builder = new StringBuilder();
		
		for (String s: arg.split("_") )
		{
			builder.append(s).append(" ");
		}
		
		return builder.toString().trim();
	}

	@Override
	public int getArity() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public ArgumentType[] getArgumentTypes() {
		return [ArgumentType.String,ArgumentType.String].toArray();
	}

}
