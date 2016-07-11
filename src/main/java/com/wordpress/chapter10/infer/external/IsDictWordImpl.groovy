package com.wordpress.chapter10.infer.external;



import java.io.File;
import java.net.URL;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;
import edu.umd.cs.psl.database.ReadOnlyDatabase;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm
import edu.umd.cs.psl.model.function.ExternalFunction;

public class IsDictWordImpl implements ExternalFunction {
	
	static final String WNHOME = "/home/ispace/data/wordnet/WordNet-3.0";
	
	private IDictionary dict;
	
	public IsDictWordImpl() {
		String path = WNHOME + File.separator + "dict";
		URL url = new URL("file" , null, path);
	    dict = new Dictionary(url);
		dict.open();
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

		String type = localName(args[0].toString().trim());

		if (isWord(type)) {
			
			//System.out.println(type + "is a dictionary word");
			return 1.0;
		} else {
			return 0.0;
		}
	}

	private static String localName(String uri) {
		try {
			URI uri0 = (new ValueFactoryImpl()).createURI(uri);
			
			return uri0.getLocalName();
		} catch (IllegalArgumentException ex) {
			return uri;
		}

	}
	
	private boolean isWord(String word) {
		
		IIndexWord idxWord = dict.getIndexWord(word.toLowerCase(), POS.NOUN);
				
		if (idxWord == null) {
			return false;
		} else {
			return true;
		}
	}

}
