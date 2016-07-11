package com.wordpress.chapter10.test

import java.util.Map;

import com.wordpress.chapter10.infer.external.MetaPredicateImpl;
import com.wordpress.chapter10.infer.external.MetaTypeImpl;
import com.wordpress.chapter10.infer.external.YagoTypeImpl;
import com.wordpress.chapter10.util.PSLUtil;

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
import edu.umd.cs.psl.model.argument.Variable
import edu.umd.cs.psl.model.atom.GroundAtom;
import edu.umd.cs.psl.model.atom.RandomVariableAtom
import edu.umd.cs.psl.model.function.ExternalFunction;
import edu.umd.cs.psl.model.parameters.Weight;
import edu.umd.cs.psl.model.predicate.Predicate;
import edu.umd.cs.psl.ui.functions.textsimilarity.*;
import edu.umd.cs.psl.ui.loading.InserterUtils;
import edu.umd.cs.psl.util.database.Queries;
import groovy.time.*;

ConfigManager configManager = ConfigManager.getManager();
ConfigBundle configBundle = configManager.getBundle("feedbacktest1");

String defaultPath  = System.getProperty("user.dir");

String dbpath       = configBundle.getString("dbpath",
		defaultPath + File.separator + "feedbacktest1");

// clearDB is false so that we don't delete previous data
DataStore dataStore = new RDBMSDataStore(
		new H2DatabaseDriver(Type.Disk, dbpath, true),configBundle);
	
	
PSLModel m = new PSLModel(this, dataStore);

m.add predicate: "CandSimEntityType",  types: [ArgumentType.String,ArgumentType.String];
m.add predicate: "Feedback2",  types: [ArgumentType.String,ArgumentType.String,ArgumentType.String,ArgumentType.String];
m.add predicate: "SimEntityType", types: [ArgumentType.String, ArgumentType.String];


m.add rule: (CandSimEntityType(T1,T2) & Feedback2(T1,T2,"yes","type_equivalence")) >> SimEntityType(T1,T2), weight: 10.0;
m.add rule: (~CandSimEntityType(T1,T2) & Feedback2(T1,T2,"yes","type_equivalence")) >> SimEntityType(T1,T2), weight: 1.0;
m.add rule: (CandSimEntityType(T1,T2) &  Feedback2(T1,T2,"no","type_equivalence")) >> ~SimEntityType(T1,T2), weight: 1.0;
m.add rule: (~CandSimEntityType(T1,T2) & Feedback2(T1,T2,"no","type_equivalence")) >> ~SimEntityType(T1,T2), weight: 10.0;

//m.add rule: (SimEntityType(T1,T2) & Feedback2(T1,T2,"yes","type_equivalence")) >> CandSimEntityType(T1,T2), weight: 10.0;
//m.add rule: (SimEntityType(T1,T2) & Feedback2(T1,T2,"yes","type_equivalence")) >> ~CandSimEntityType(T1,T2), weight: 10.0;
//m.add rule: (~SimEntityType(T1,T2) & Feedback2(T1,T2,"no","type_equivalence")) >> CandSimEntityType(T1,T2) , weight: 10.0;
//m.add rule: (~SimEntityType(T1,T2) & Feedback2(T1,T2,"no","type_equivalence")) >> ~CandSimEntityType(T1,T2), weight: 10.0;

String dataroot = "data/feedback"

// load data
// observed data maps
def observedMap = [
	((Predicate)Feedback2):dataroot+"/Feedback2.csv"];

def observedMapTruth = [
	((Predicate)CandSimEntityType):[dataroot+"/CandSimEntityType.csv"]];

Partition observedPart = new Partition(100);



PSLUtil.loadFromCSV(dataStore, observedMap, observedPart);
PSLUtil.loadFromCSVWithTruthValue(dataStore, observedMapTruth, observedPart);

//// now we do the inference
Partition inferPart = new Partition(2);

dataStore.getInserter(SimEntityType, inferPart).insertValue(0.0,"T1","T2")
dataStore.getInserter(SimEntityType, inferPart).insertValue(0.0,"T3","T4")
dataStore.getInserter(SimEntityType, inferPart).insertValue(0.0,"T5","T6")
dataStore.getInserter(SimEntityType, inferPart).insertValue(0.0,"T7","T8")

System.out.println "[INFO]: running inference...";

def closedWorldPredicateSet = [CandSimEntityType, Feedback2] as Set;

Database inferredDB = dataStore.getDatabase(inferPart, closedWorldPredicateSet, observedPart);
MPEInference inference = new MPEInference(m, inferredDB, configBundle);
inference.mpeInference();
inference.close();
//inferredDB.close();

def predicateFileMap = [
	((Predicate)m.getPredicate("SimEntityType")):dataroot+"/SimEntityType.csv"];

//printResults(dataStore, inferPart, predicateFileMap);

//System.out.println("[INFO]: inference finished in");
for (GroundAtom atom : Queries.getAllAtoms(inferredDB, m.getPredicate("SimEntityType")))
	println atom.toString() + "\t" + atom.getValue();


inferredDB.close();

def printResults(DataStore dataStore, Partition partition, Map predicateFileMap) {
	predicateFileMap.each {predicate, file ->
		PSLUtil.printCSVResults(dataStore, partition, predicate, file, false);
	}
}