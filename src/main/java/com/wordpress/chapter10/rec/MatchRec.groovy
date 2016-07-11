package com.wordpress.chapter10.rec


import java.util.Map;

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
import edu.umd.cs.psl.model.predicate.Predicate;
import edu.umd.cs.psl.ui.functions.textsimilarity.*;
import edu.umd.cs.psl.ui.loading.InserterUtils;
import edu.umd.cs.psl.util.database.Queries;
import groovy.time.*;

String dataroot = args[0];


// load the configuration manager
ConfigManager cm = ConfigManager.getManager();
ConfigBundle config = cm.getBundle("schemainfer");


// Use and H2 DataStore and stores it in the user working directory.
String defaultPath  = System.getProperty("user.dir");

String dbpath       = config.getString("dbpath",
		defaultPath + File.separator + "idrec");

// store data on disk
DataStore dataStore = new RDBMSDataStore(
		new H2DatabaseDriver(Type.Disk, dbpath, true),config);

// create the PSL model
PSLModel m = new PSLModel(this, dataStore);


// evidence predicates
m.add predicate: "Entity",  types: [ArgumentType.String];
m.add predicate: "Property",  types: [ArgumentType.String];
m.add predicate: "FromOnt",  types: [
	ArgumentType.String,
	ArgumentType.String
];
m.add predicate: "HasDomain", types: [
	ArgumentType.String,
	ArgumentType.String
];
m.add predicate: "HasPropertyValue", types: [
	ArgumentType.String,
	ArgumentType.String
];
m.add predicate: "Label", types: [
	ArgumentType.String,
	ArgumentType.String
];
m.add predicate: "PropertyValue", types: [ArgumentType.String];

// un-observed similarity
m.add predicate: "SimPropertyValue", types: [
	ArgumentType.String,
	ArgumentType.String
];


// query predicate
m.add predicate: "SameEntity", types: [
	ArgumentType.String,
	ArgumentType.String
];



// external function
m.add function: "fnEidtDistSim" , implementation: new EditDistSim();
m.add function: "fnNMWunschSim" , implementation: new NMWunschSim();
m.add function: "fnJaccardSim" , implementation: new JaccardSim();
m.add function: "fnWMSmithSim" , implementation: new WMSmithSim();


//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & HasPropertyValue(E1,V1) & HasPropertyValue(E2,V2) & fnEidtDistSim(V1,V2)) >> SimPropertyValue(V1,V2), weight: 1.0;
//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & HasPropertyValue(E1,V1) & HasPropertyValue(E2,V2) & fnJaccardSim(V1,V2)) >> SimPropertyValue(V1,V2), weight: 10.0;
//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & HasPropertyValue(E1,V1) & HasPropertyValue(E2,V2) & fnNMWunschSim(V1,V2)) >> SimPropertyValue(V1,V2), weight: 10.0;
m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & HasPropertyValue(E1,V1) & HasPropertyValue(E2,V2) & fnWMSmithSim(V1,V2)) >> SimPropertyValue(V1,V2), weight: 5;

//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & HasPropertyValue(E1,V1) & HasPropertyValue(E2,V2) & ~fnEidtDistSim(V1,V2)) >> ~SimPropertyValue(V1,V2), weight: 2.0;
//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & HasPropertyValue(E1,V1) & HasPropertyValue(E2,V2) & ~fnJaccardSim(V1,V2)) >> ~SimPropertyValue(V1,V2), weight: 20.0;
//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & HasPropertyValue(E1,V1) & HasPropertyValue(E2,V2) & ~fnNMWunschSim(V1,V2)) >> ~SimPropertyValue(V1,V2), weight: 20.0;
//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & HasPropertyValue(E1,V1) & HasPropertyValue(E2,V2) & ~fnWMSmithSim(V1,V2)) >> ~SimPropertyValue(V1,V2), weight: 10;

//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") &
//	Label(E1,L1) & Label(E2,L2) &
//	fnEidtDistSim(L1,L2)) >> SimPropertyValue(L1,L2), weight: 1.0;

//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") &
//	Label(E1,L1) & Label(E2,L2) &
//	fnNMWunschSim(L1,L2)) >> SimPropertyValue(L1,L2), weight: 10.0;

//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") &
//	Label(E1,L1) & Label(E2,L2) &
//	fnJaccardSim(L1,L2)) >> SimPropertyValue(L1,L2), weight: 10.0;

m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") &
	Label(E1,L1) & Label(E2,L2) &
	fnWMSmithSim(L1,L2)) >> SimPropertyValue(L1,L2), weight: 20.0;

/*
m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") &
			 Label(E1,L1) & Label(E2,L2) &
			 fnEidtDistSim(L1,L2)) >> SimPropertyValue(L1,L2), weight: 1.0;
		 
m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") &
			 Label(E1,L1) & Label(E2,L2) &
			 fnNMWunschSim(L1,L2)) >> SimPropertyValue(L1,L2), weight: 10.0;
		 
m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") &
			 Label(E1,L1) & Label(E2,L2) &
			 fnJaccardSim(L1,L2)) >> SimPropertyValue(L1,L2), weight: 10.0;
		 
m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") &
			 Label(E1,L1) & Label(E2,L2) &
			 fnWMSmithSim(L1,L2)) >> SimPropertyValue(L1,L2), weight: 20.0;
		 
m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") &
			 Label(E1,L1) & Label(E2,L2) &
			 ~fnEidtDistSim(L1,L2)) >> ~SimPropertyValue(L1,L2), weight: 2.0;
		 
m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") &
			 Label(E1,L1) & Label(E2,L2) &
			 ~fnNMWunschSim(L1,L2)) >> ~SimPropertyValue(L1,L2), weight: 20.0;
		 
m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") &
			 Label(E1,L1) & Label(E2,L2) &
			 ~fnJaccardSim(L1,L2)) >> ~SimPropertyValue(L1,L2), weight: 20.0;
		 
m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") &
			 Label(E1,L1) & Label(E2,L2) &
			 ~fnWMSmithSim(L1,L2)) >> ~SimPropertyValue(L1,L2), weight: 10.0;
*/

//m.add rule: ~SimPropertyValue(V1,V2), weight: 10;

// SimPropertyValue
//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & 
//	         HasPropertyValue(E1,V1) & HasPropertyValue(E2,V2) & 
//			 fnEidtDistSim(V1,V2)) >> SimPropertyValue(V1,V2), weight: 5.0;

//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & 
//	         HasPropertyValue(E1,V1) & HasPropertyValue(E2,V2) & 
//			 HasDomain(V1,P) & HasDomain(V2,P) & 
//			 fnEidtDistSim(V1,V2)) >> SimPropertyValue(V1,V2), weight: 5.0;

//m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") &
//			 Label(E1,L1) & Label(E2,L2) &
//			 fnEidtDistSim(L1,L2)) >> SimPropertyValue(L1,L2), weight: 5.0;
		 



// observed data maps
def observedMap = [
	((Predicate)Entity):dataroot+"/Entity.csv",
	((Predicate)Property):dataroot+"/Property.csv",
	((Predicate)FromOnt):dataroot+"/FromOnt.csv",
	((Predicate)Label):dataroot+"/Label.csv",
	((Predicate)HasDomain):dataroot+"/HasDomain.csv",
	((Predicate)HasPropertyValue):dataroot+"/HasPropertyValue.csv",
	((Predicate)m.getPredicate("PropertyValue")):dataroot+"/PropertyValue.csv"];


Partition observedPart = new Partition(100);

Date start = new Date();

PSLUtil.loadFromCSV(dataStore, observedMap, observedPart);

Date stop = new Date();

TimeDuration td = TimeCategory.minus( stop, start );

System.out.println "[INFO]: data loading finished in " + td;



def closedWorldPredicateSet = [
	Entity,
	Property,
	FromOnt,
	Label,
	HasPropertyValue,
	HasDomain,
	HasProperty,
	(Predicate)m.getPredicate("PropertyValue")] as Set;


//// now we do the inference
Partition inferPart1 = new Partition(200);

Partition inferPart2 = new Partition(300);

System.out.println "[INFO]: running inference...";

start = new Date();

Database inferredDB = dataStore.getDatabase(inferPart1,
		closedWorldPredicateSet,
		observedPart);

LazyMPEInference inference = new LazyMPEInference(m, inferredDB, config);
inference.mpeInference();
inference.close();
inferredDB.close();

def predicateFileMap = [
	((Predicate)
		m.getPredicate("SimPropertyValue")):dataroot+"/SimPropertyValue.csv"];

printResults(dataStore, inferPart1, predicateFileMap);


///////////////////////////////////////////////////////////////////////////////
// run 2nd round of inference


// set similarity
m.add setcomparison: "SimValueSet" ,
	  using: SetComparison.CrossEquality, on : SimPropertyValue;
	  	  
m.add rule: (FromOnt(E1,"A") & FromOnt(E2,"B") & SimValueSet({E1.HasPropertyValue},{E2.HasPropertyValue})) >> SameEntity(E1,E2), weight: 1.0;

//m.add rule: (Entity(E1) & Entity(E2) & SimValueSet({E1.HasPropertyValue},{E2.HasPropertyValue})) >> SameEntity(E1,E2), weight: 1.0;

//m.add rule: (fnEidtDistSim(L1,L2) & FromOnt(E1,"A") & FromOnt(E2,"B") & Label(E1,L1) & Label(E1,L2)) >> SameEntity(E1,E2), weight: 1.0;

//m.add rule : ~SameEntity(E1,E2), weight: 1;

closedWorldPredicateSet = [
	Entity,
	Property,
	FromOnt,
	Label,
	HasPropertyValue,
	HasDomain,
	HasProperty,
	SimPropertyValue] as Set;

Database inferredDB2 = dataStore.getDatabase(inferPart2, closedWorldPredicateSet,
		observedPart, inferPart1);
	
	
inference = new LazyMPEInference(m, inferredDB2, config);
inference.mpeInference();
inference.close();
inferredDB2.close();

stop = new Date();

td = TimeCategory.minus( stop, start );

System.out.println("[INFO]: inference finished in " + td);

predicateFileMap = [
	((Predicate)
		m.getPredicate("SameEntity")):dataroot+"/SameEntity.csv"];

printResults(dataStore, inferPart2, predicateFileMap);


///////////////////////////////////////////////////////////////////////////////

def printResults(DataStore dataStore, 
	             Partition partition, 
	             Map predicateFileMap) {
	
	predicateFileMap.each {predicate, file ->
		PSLUtil.printCSVResults(dataStore, partition, predicate, file, false);
	}
}