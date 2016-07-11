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
ConfigBundle configBundle = configManager.getBundle("setsim");

String defaultPath  = System.getProperty("user.dir");

String dbpath       = configBundle.getString("dbpath",
		defaultPath + File.separator + "setsim");

// clearDB is false so that we don't delete previous data
DataStore dataStore = new RDBMSDataStore(
		new H2DatabaseDriver(Type.Disk, dbpath, true),configBundle);
	
	
PSLModel m = new PSLModel(this, dataStore);

m.add predicate: "Entity",  types: [ArgumentType.String];
m.add predicate: "Seti",  types: [ArgumentType.String];
m.add predicate: "HasMember", types: [ArgumentType.String, ArgumentType.String];
m.add predicate: "SimMember", types: [ArgumentType.String, ArgumentType.String];
m.add predicate: "Sim", types: [ArgumentType.String, ArgumentType.String];

m.add setcomparison: "SimSet" , using: SetComparison.CrossEquality, on : SimMember;

m.add rule: (Seti(S1) & Seti(S2) & SimSet({S1.HasMember},{S2.HasMember})) >> Sim(S1,S2), constraint: true;
m.add rule: (Seti(E)) >> Sim(E,E), constraint: true;
m.add rule: (Entity(E)) >> SimMember(E,E), constraint: true;
m.add rule: (SimMember(E1,E2)) >> SimMember(E2,E1), constraint: true;
m.add rule: (SimMember(E1,E2) & SimMember(E2,E3)) >> SimMember(E1,E3), constraint: true;

String dataroot = "data/setsim"

// load data
// observed data maps
def observedMap = [
	((Predicate)Entity):dataroot+"/Entity.csv",
	((Predicate)Seti):dataroot+"/Seti.csv"];

def observedMapTruth = [
	((Predicate)SimMember):[dataroot+"/SimMember.csv"],
	((Predicate)HasMember):[dataroot+"/HasMember.csv"]];

Partition observedPart = new Partition(100);

PSLUtil.loadFromCSV(dataStore, observedMap, observedPart);
PSLUtil.loadFromCSVWithTruthValue(dataStore, observedMapTruth, observedPart);

//// now we do the inference
Partition inferPart = new Partition(200);


System.out.println "[INFO]: running inference...";

def closedWorldPredicateSet = [ HasMember, SimMember, Seti, Entity] as Set;

Database inferredDB = dataStore.getDatabase(inferPart, closedWorldPredicateSet, observedPart);
LazyMPEInference inference = new LazyMPEInference(m, inferredDB, configBundle);
inference.mpeInference();
inference.close();
//inferredDB.close();

def predicateFileMap = [
	((Predicate)m.getPredicate("Sim")):dataroot+"/Sim.csv"];

//printResults(dataStore, inferPart, predicateFileMap);

//System.out.println("[INFO]: inference finished in");
for (GroundAtom atom : Queries.getAllAtoms(inferredDB, m.getPredicate("Sim")))
	println atom.toString() + "\t" + atom.getValue();


inferredDB.close();

def printResults(DataStore dataStore, Partition partition, Map predicateFileMap) {
	predicateFileMap.each {predicate, file ->
		PSLUtil.printCSVResults(dataStore, partition, predicate, file, false);
	}
}