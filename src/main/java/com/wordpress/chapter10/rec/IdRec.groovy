package com.wordpress.chapter10.rec


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import uk.ac.man.cs.stdlib.PairOfStrings;

import com.wordpress.chapter10.util.MapUtil;
import com.wordpress.chapter10.util.PSLUtil;

import edu.princeton.cs.algs4.Out
import edu.princeton.cs.algs4.SET
import edu.princeton.cs.algs4.StdOut;
import edu.umd.cs.psl.application.inference.*;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.LazyMaxLikelihoodMPE;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.MaxLikelihoodMPE;
import edu.umd.cs.psl.config.*;
import edu.umd.cs.psl.database.DataStore;
import edu.umd.cs.psl.database.Database;
import edu.umd.cs.psl.database.DatabasePopulator;
import edu.umd.cs.psl.database.Partition;
import edu.umd.cs.psl.database.ReadOnlyDatabase;
import edu.umd.cs.psl.database.rdbms.RDBMSDataStore
import edu.umd.cs.psl.database.rdbms.RDBMSUniqueIntID
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver.Type
import edu.umd.cs.psl.groovy.PSLModel;
import edu.umd.cs.psl.groovy.PredicateConstraint;
import edu.umd.cs.psl.groovy.SetComparison;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm;
import edu.umd.cs.psl.model.argument.StringAttribute
import edu.umd.cs.psl.model.argument.Variable
import edu.umd.cs.psl.model.atom.GroundAtom;
import edu.umd.cs.psl.model.atom.RandomVariableAtom
import edu.umd.cs.psl.model.function.ExternalFunction;
import edu.umd.cs.psl.model.predicate.Predicate;
import edu.umd.cs.psl.model.predicate.StandardPredicate
import edu.umd.cs.psl.ui.functions.textsimilarity.*;
import edu.umd.cs.psl.ui.loading.InserterUtils;
import edu.umd.cs.psl.util.database.Queries;
import groovy.time.*;



if (args.length < 2) {
	println "Usage: java IdRec [ONT_A] [ONT_B] [GT_DATA]";
	System.exit(1);
}

String ontA = args[0];
String ontB = args[1];
String gtPath = args[2];

// pre-process the data
BuildIndex index = new BuildIndex(ontA,ontB);
Preprocess preProcessA = new Preprocess(ontA, "A");
Preprocess preProcessB = new Preprocess(ontB, "B");
Map<PairOfStrings,Double> gtData = (new LoadGroundTruth(gtPath)).getGtData();


// load the entity lists into a map
HashMap<String,Entity> entityMap = new HashMap<String,Entity>();




for (Entity ent: preProcessA.getList()) {
	entityMap.put(ent.getId(), ent);
}

for (Entity ent: preProcessB.getList()) {
	entityMap.put(ent.getId(), ent);
}

// the id-Map of entities
DualHashBidiMap<String, Long> idMap = index.getIdMap();

// get the term-postings
HashMap<String, Map<Long, Double>> postings = index.getTfIdf();

Map<String, Double> sortedIdf = MapUtil.sortByValue(index.getIdf());

Map<String,Map<String,Double>> results = new HashMap<String,Map<String,Double>>();


boolean first = true;
Out resOut = new Out("id-rec.res");

for (int i=50; i <= 3200; i=i*2) {
	int termCount = 0
	for (String term : sortedIdf.keySet()) {

		if (postings.get(term).size() > 1 && postings.get(term).size() < 100) {

			termCount++;
			
			if (termCount > i) break;
			//StdOut.printf("[%s,%.3f] -->", term, index.idf(term));

			// configure the environment

			// load the configuration manager
			ConfigManager cm = ConfigManager.getManager();
			ConfigBundle config = cm.getBundle("IDRec");

			// Use and H2 DataStore and stores it in the user working directory.
			String defaultPath  = System.getProperty("user.dir");

			String dbpath       = config.getString("dbpath",
					defaultPath + File.separator + "idrec");

			// store data on disk
			DataStore dataStore = new RDBMSDataStore(
					new H2DatabaseDriver(Type.Memory, dbpath, true),config);

			// create the PSL model
			PSLModel m = new PSLModel(this, dataStore);

			// evidence predicates
			m.add predicate: "Entity",  types: [ArgumentType.UniqueID];
			m.add predicate: "FromOnt",  types: [
				ArgumentType.UniqueID,
				ArgumentType.String
			];
			m.add predicate: "HasPropertyValue", types: [
				ArgumentType.UniqueID,
				ArgumentType.String
			];

			//m.add predicate: "PropertyValue", types: [ArgumentType.String];

			// un-observed similarity
			m.add predicate: "SimPropertyValue", types: [
				ArgumentType.String,
				ArgumentType.String
			];


			// query predicate
			m.add predicate: "SimEntity", types: [
				ArgumentType.UniqueID,
				ArgumentType.UniqueID
			];

			m.add setcomparison: "SimValueSet" ,
			using: SetComparison.CrossEquality, on : SimPropertyValue;

			if (first) {
				// external function
				m.add function: "fnEidtDistSim" , implementation: new EditDistSim();
				m.add function: "fnNMWunschSim" , implementation: new NMWunschSim();
				m.add function: "fnJaccardSim" , implementation: new JaccardSim();
				m.add function: "fnCosineSim" , implementation: new JaccardSim();
				m.add function: "fnWMSmithSim" , implementation: new WMSmithSim();
				first = false;
			}

			m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & HasPropertyValue(E1,V1) & HasPropertyValue(E2,V2) & fnEidtDistSim(V1,V2)) >> SimEntity(E1,E2), weight: 10.0;
			//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & HasPropertyValue(E1,V1) & HasPropertyValue(E2,V2) & fnJaccardSim(V1,V2)) >> SimPropertyValue(V1,V2), weight: 10.0;
			//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & HasPropertyValue(E1,V1) & HasPropertyValue(E2,V2) & fnNMWunschSim(V1,V2)) >> SimPropertyValue(V1,V2), weight: 10.0;
			m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & HasPropertyValue(E1,V1) & HasPropertyValue(E2,V2) & fnWMSmithSim(V1,V2)) >> SimEntity(E1,E2), weight: 10.0;
			m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & HasPropertyValue(E1,V1) & HasPropertyValue(E2,V2) & fnCosineSim(V1,V2)) >> SimEntity(E1,E2), weight: 10.0;

			//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & SimValueSet({E1.HasPropertyValue},{E2.HasPropertyValue})) >> SimEntity(E1,E2), weight: 10.0;
			m.add rule : ~SimEntity(E1,E2), weight: 10;

			// create an observed partition
			Partition observedPart = new Partition(100);

			def ent = dataStore.getInserter(
					(StandardPredicate)m.getPredicate("Entity"), observedPart);
			def frmOnt = dataStore.getInserter(
					(StandardPredicate)m.getPredicate("FromOnt"), observedPart);
			def hpv = dataStore.getInserter(
					(StandardPredicate)m.getPredicate("HasPropertyValue"), observedPart);

			for (Entry<Long, Double> entry : postings.get(term).entrySet()) {

				//	StdOut.printf("(%s, %.3f) ", idMap.getKey(entry.getKey()),
				//			entry.getValue());

				def entityId = dataStore.getUniqueID(entry.getKey());
				String entityStr = idMap.getKey(entry.getKey());
				Entity entity = entityMap.get(entityStr);

				ent.insert(entityId);
				frmOnt.insert(entityId,entity.getOntLabel());

				for (String pv: entity.getPropertyValues()) {
					hpv.insert(entityId,pv);
				}
			}

			Partition inferPart = new Partition(301);

			Database inferredDB = dataStore.getDatabase(inferPart,
					[
						(Predicate)m.getPredicate("Entity"),
						(Predicate)m.getPredicate("FromOnt"),
						(Predicate)m.getPredicate("HasPropertyValue")] as Set,
					observedPart);

			LazyMPEInference inference = new LazyMPEInference(m, inferredDB, config);
			inference.mpeInference();
			inference.close();
			inferredDB.close();

			// post process the results

			Partition dummy = new Partition(99);

			Database resultsDB = dataStore.getDatabase(dummy, inferPart);

			Set atomSet = Queries.getAllAtoms(resultsDB,SimEntity);

			def resultsMap = [:]
			for (GroundAtom a : atomSet) {
				//out.println(atomToString(a))
				resultsMap.put(a,a.value);
				//System.out.println(a + "," + a.value.toString());
			}

			Map sortedMap = resultsMap.sort {a, b -> b.value <=> a.value}


			sortedMap.each {atom, value ->
				Long e1 = Long.parseLong(atom.arguments[0].toString());
				Long e2 = Long.parseLong(atom.arguments[1].toString());

				String ent1 = idMap.getKey(e1);
				String ent2 = idMap.getKey(e2);

				if (results.containsKey(ent1)) {
					results.get(ent1).putAt(ent2,value);
				} else {
					Map<String,Double> pairedMap = new HashMap<String,Double>();
					pairedMap.put(ent2, value);
					results.put(ent1, pairedMap);
				}
			}
		}

	}

	Out fOut = new Out("data/im-identity/im-identity-out.csv");


	int tp = 0;
	int fp = 0;
	int p = gtData.size();

	results.each{ent1,pairdMap ->
		Map sortedPairdMap = pairdMap.sort {a, b -> b.value <=> a.value};

		Map.Entry<String, Double> entry = sortedPairdMap.entrySet().iterator().next();

		int gt = (gtData.containsKey(new PairOfStrings(ent1, entry.key)) ? 1 : 0);

		if (gt == 1)
			tp++;
		else
			fp++;

		//StdOut.printf("%s,%s,%.5f,%s\n",ent1, entry.key,entry.value, gt);
		fOut.printf("%s,%s,%.5f,%s\n",ent1, entry.key,entry.value, gt);
	}

	fOut.close();

	// compute prec and recall
	double prec = (double)tp/(tp+fp);
	double rec  = (double)tp/p;

	resOut.printf("#terms = %d, Prec. = %.4f, Rec. = %.4f\n", termCount, prec,rec);

	fOut = new Out("data/im-identity/im-identity-gt.csv");

	gtData.each{pair,value ->
		fOut.printf("%s,%s,%.5f\n",pair.first, pair.second,value);
	}

	fOut.close();

}

resOut.close();


def String predicateString(GroundAtom atom) {
	StringBuilder sb = new StringBuilder();

	sb.append(atom.getPredicate().getName().toUpperCase()).append("(");

	int numOfArgs = atom.arguments.length;

	for (int i=0; i < numOfArgs - 1; i++) {
		//sb.append(atom.arguments[i].getValue()).append(", ");

		if (atom.arguments[i] instanceof StringAttribute) {
			sb.append(atom.arguments[i].getValue()).append(", ");
		} else {
			sb.append(atom.arguments[i].toString()).append(", ");
		}

	}

	//sb.append(atom.arguments[numOfArgs-1].getValue()).append(")");
	//sb.append(atom.arguments[numOfArgs-1].toString()).append(")");

	if (atom.arguments[numOfArgs-1] instanceof StringAttribute) {
		sb.append(atom.arguments[numOfArgs-1].getValue()).append(")");
	} else {
		sb.append(atom.arguments[numOfArgs-1].toString()).append(")");
	}


	return sb.toString();
}

