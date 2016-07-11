/**
 * FileName: LoadData.groovy
 * Date    : 13-4-2015
 * 
 * @author Duhai Alshukaili
 * 
 * A groovy class for loading observed data that are extracted from
 * the pre-processing setp.
 */

package com.wordpress.chapter10;


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

Date start = new Date();

//Where the data resides (first argument to this script)
String dataroot = args[0];

String rdfInput = dataroot + File.separator + "rdf" + File.separator;
String feedbackInput = dataroot + File.separator + "feedback" + File.separator;
String ontInput = dataroot + File.separator + "ont" + File.separator;
String gtInput = dataroot + File.separator + "gt" + File.separator;
String output = dataroot + File.separator + "output" + File.separator;
//String dbfiles = dataroot + File.separator + "db" + File.separator;
String dbfiles = "/media/black/";
// load the configuration manager
ConfigManager cm = ConfigManager.getManager();

ConfigBundle config = cm.getBundle("ldpaygo2");
config.addProperty("rdbmsdatastore.usestringids", true);

// Use and H2 DataStore and stores it in the user working directory.
String defaultPath  = System.getProperty("user.dir");

String dbpath       = config.getString("dbpath",
	dbfiles + "ldpaygo2");

// store data on disk	
DataStore dataStore = new RDBMSDataStore(
	new H2DatabaseDriver(Type.Disk, dbpath, true),config);

// create the PSL model
PSLModel model = new PSLModel(this, dataStore);
	
////////////////////////// predicate declaration ////////////////////////
System.out.println "[info] \t\tDECLARING PREDICATES...";

/** Observed predicates **/
model.add predicate: "Triple1",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID, ArgumentType.UniqueID];

model.add predicate: "Triple2",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID, ArgumentType.UniqueID];

model.add predicate: "ShortStringLit",  types: [ArgumentType.UniqueID];

model.add predicate: "DateLit",  types: [ArgumentType.UniqueID];

model.add predicate: "NumericLit",  types: [ArgumentType.UniqueID];

model.add predicate: "LongStringLit",  types: [ArgumentType.UniqueID];

model.add predicate: "RawType", types: [ArgumentType.UniqueID];

model.add predicate: "RawPredicate", types: [ArgumentType.UniqueID];

model.add predicate: "RawSubject", types: [ArgumentType.UniqueID];

model.add predicate: "RawObject", types: [ArgumentType.UniqueID];

// EntityTypeFB(T, UID, Term)
model.add predicate: "EntityTypeFB", types: [
	ArgumentType.UniqueID,
	ArgumentType.UniqueID,
	ArgumentType.String
];


//HasTypeFB(E, T, UID, Term)
model.add predicate: "HasTypeFB", types: [
	ArgumentType.UniqueID,
	ArgumentType.UniqueID,
	ArgumentType.UniqueID,
	ArgumentType.String
];


// HasPropertyFB(T, P, UID, Term)
model.add predicate: "HasPropertyFB", types: [
	ArgumentType.UniqueID,
	ArgumentType.UniqueID,
	ArgumentType.UniqueID,
	ArgumentType.String
];


//SimPropertyFB(P1, P2, UID, Term)
model.add predicate: "SimPropertyFB", types: [
	ArgumentType.UniqueID,
	ArgumentType.UniqueID,
	ArgumentType.UniqueID,
	ArgumentType.String
];


//SimEntityTypeFB(T1, T2, UID, Term)
model.add predicate: "SimEntityTypeFB", types: [
	ArgumentType.UniqueID,
	ArgumentType.UniqueID,
	ArgumentType.UniqueID,
	ArgumentType.String
];


//SimEntityFB(T1, T2, UID, Term)
model.add predicate: "SimEntityFB", types: [
	ArgumentType.UniqueID,
	ArgumentType.UniqueID,
	ArgumentType.UniqueID,
	ArgumentType.String
];

/*
model.add predicate: "Feedback1", types: [
	ArgumentType.UniqueID,
	ArgumentType.UniqueID,
	ArgumentType.String,
	ArgumentType.String
];

model.add predicate: "Feedback2", types: [
	ArgumentType.UniqueID,
	ArgumentType.UniqueID,
	ArgumentType.UniqueID,
	ArgumentType.String,
	ArgumentType.String
];
*/

// OntologyTriples 
model.add predicate: "OntTriple",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID, ArgumentType.UniqueID];


//model.add predicate: "LocalName",  types: [ArgumentType.UniqueID, ArgumentType.String];

// model.add predicate: "SimURI",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID];

// model.add predicate: "SimLit",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID];

// model.add predicate: "LexPropertyValueSim",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID];


// load observed atoms 
def observedMap = [
	((Predicate)Triple1):rdfInput+"/triple1.csv",
	((Predicate)Triple2):rdfInput+"/triple2.csv",
	((Predicate)ShortStringLit):rdfInput+"/shortStringLit.csv",
	((Predicate)LongStringLit):rdfInput+"/longStringLit.csv",
	((Predicate)DateLit):rdfInput+"/dateLit.csv",
	
	// raw data
	((Predicate)RawType):rdfInput+"/rdftype.csv",
	((Predicate)RawSubject):rdfInput+"/subject.csv",
	((Predicate)RawPredicate):rdfInput+"/predicate.csv",
	((Predicate)RawObject):rdfInput+"/object.csv",
	
	// feedback data
	((Predicate)EntityTypeFB):feedbackInput+"/EntityTypeFB.csv",
	((Predicate)HasTypeFB):feedbackInput+"/HasTypeFB.csv",
	((Predicate)HasPropertyFB):feedbackInput+"/HasPropertyFB.csv",
	((Predicate)SimPropertyFB):feedbackInput+"/SimPropertyFB.csv",
	((Predicate)SimEntityTypeFB):feedbackInput+"/SimEntityTypeFB.csv",
	((Predicate)SimEntityFB):feedbackInput+"/SimEntityFB.csv"];

	//((Predicate)Feedback1):dataroot+"/feedback1.csv",
	//((Predicate)Feedback2):dataroot+"/feedback2.csv",];

/*
def observedMapTruth = [
	((Predicate)SimURI):[dataroot+"/SimURI.csv"],
	((Predicate)SimLit):[dataroot+"/SimLit.csv"],
	//((Predicate)LexPropertyValueSim):[dataroot+"/SimLit.csv",dataroot+"/SimURI.csv"] 
	((Predicate)LexPropertyValueSim):[dataroot+"/SimLit.csv"]
	]; 
*/

Partition observedPart = new Partition(100);

PSLUtil.loadFromCSV(dataStore, observedMap, observedPart);
// PSLUtil.loadFromCSVWithTruthValue(dataStore, observedMapTruth, observedPart);

Date stop = new Date();

TimeDuration td = TimeCategory.minus( stop, start );

System.out.println "[info] data loading finished in " + td;








	

