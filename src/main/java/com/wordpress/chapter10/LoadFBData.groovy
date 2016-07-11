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

// load the configuration manager
ConfigManager cm = ConfigManager.getManager();
ConfigBundle config = cm.getBundle("ldpaygo2");


// Use and H2 DataStore and stores it in the user working directory.
String defaultPath  = System.getProperty("user.dir");

String dbpath       = config.getString("dbpath",
		defaultPath + File.separator + "ldpaygo2");

// store data on disk	
DataStore dataStore = new RDBMSDataStore(
	new H2DatabaseDriver(Type.Disk, dbpath, false),config);

// create the PSL model
PSLModel model = new PSLModel(this, dataStore);
	
////////////////////////// predicate declaration ////////////////////////
System.out.println "[info] \t\tDECLARING PREDICATES...";

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




def observedMap = [
	((Predicate)EntityTypeFB):feedbackInput+"/EntityTypeFB.csv",
	((Predicate)HasTypeFB):feedbackInput+"/HasTypeFB.csv",
	((Predicate)HasPropertyFB):feedbackInput+"/HasPropertyFB.csv",
	((Predicate)SimPropertyFB):feedbackInput+"/SimPropertyFB.csv",
	((Predicate)SimEntityTypeFB):feedbackInput+"/SimEntityTypeFB.csv",
	((Predicate)SimEntityFB):feedbackInput+"/SimEntityFB.csv"];

Partition observedPart = new Partition(100);

PSLUtil.loadFromCSV(dataStore, observedMap, observedPart);

Date stop = new Date();

TimeDuration td = TimeCategory.minus( stop, start );

System.out.println "[info] feedback data loading finished in " + td;






	

