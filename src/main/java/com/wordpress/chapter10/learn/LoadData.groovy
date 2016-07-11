/**
 * FileName: LoadData.groovy
 * Date    : 13-4-2015
 * 
 * @author Duhai Alshukaili
 * 
 * A groovy class for loading observed data that are extracted from
 * the pre-processing setp.
 */

package com.wordpress.chapter10.learn;


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


// The ground truth path
String gtPath = args[0];



// load the configuration manager
ConfigManager cm = ConfigManager.getManager();
ConfigBundle config = cm.getBundle("ldpaygo2");


// Use and H2 DataStore and stores it in the user working directory.
String defaultPath  = System.getProperty("user.dir");

String dbpath       = config.getString("dbpath",
		defaultPath + File.separator + "ldpaygo2");

// store data on disk	
DataStore dataStore = new RDBMSDataStore(
	new H2DatabaseDriver(Type.Disk, dbpath, true),config);

// create the PSL model
PSLModel model = new PSLModel(this, dataStore);
	
////////////////////////// predicate declaration ////////////////////////

/** Observed predicates **/
model.add predicate: "Triple1",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID, ArgumentType.UniqueID];

model.add predicate: "Triple2",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID, ArgumentType.UniqueID];

model.add predicate: "SimURI",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID];

model.add predicate: "SimLit",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID];

model.add predicate: "LexPropertyValueSim",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID];


/** Target Model Predicates **/
model.add predicate: "Entity",  types: [ArgumentType.UniqueID];

model.add predicate: "EntityType",  types: [ArgumentType.UniqueID];

model.add predicate: "Property",  types: [ArgumentType.UniqueID];

model.add predicate: "PropertyValue",  types: [ArgumentType.UniqueID];

model.add predicate: "HasPropertyValue",  types: [ArgumentType.UniqueID,ArgumentType.UniqueID];

model.add predicate: "HasDomain",  types: [ArgumentType.UniqueID,ArgumentType.UniqueID];
	
model.add predicate: "HasType",  types: [ArgumentType.UniqueID,ArgumentType.UniqueID];

model.add predicate: "HasProperty", types: [ArgumentType.UniqueID, ArgumentType.UniqueID];

model.add predicate: "SimPropertyValue", types: [ArgumentType.UniqueID, ArgumentType.UniqueID];

model.add predicate: "SimEntityType",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID];

model.add predicate: "SimEntity",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID];

model.add predicate: "SimProperty", types: [ArgumentType.UniqueID,ArgumentType.UniqueID];


// observed data partition
Partition observedPart = new Partition(100);

// load triples
def triplesMap = [
	((Predicate)Triple1):gtPath+"/triple1.csv",
	((Predicate)Triple2):gtPath+"/triple2.csv" ];

PSLUtil.loadFromCSV(dataStore, triplesMap, observedPart);

def observedMapTruth = [
	((Predicate)SimURI):[gtPath+"/SimURI.csv"],
	((Predicate)SimLit):[gtPath+"/SimLit.csv"],
	//((Predicate)LexPropertyValueSim):[dataroot+"/SimLit.csv",dataroot+"/SimURI.csv"] 
	((Predicate)LexPropertyValueSim):[gtPath+"/SimLit.csv"]
	]; 

PSLUtil.loadFromCSVWithTruthValue(dataStore, observedMapTruth, observedPart);

Date stop = new Date();

TimeDuration td = TimeCategory.minus( stop, start );

System.out.println "[info] data loading finished in " + td;






	

