/**
 * FileName: LoadOntologyData.groovy
 * Date    : 14-7-2015
 * 
 * @author Duhai Alshukaili
 * 
 * A groovy class for loading Ontology triples.
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

//  The observed data path
String ontTriplesPath = args[0];

// load the configuration manager
ConfigManager cm = ConfigManager.getManager();
ConfigBundle config = cm.getBundle("ldpaygo2");


// Use and H2 DataStore and stores it in the user working directory.
String defaultPath  = System.getProperty("user.dir");

// String dbpath       = config.getString("dbpath",
//		defaultPath + File.separator + "ldpaygo2");



 String dbpath       = config.getString("dbpath", "data1/people/tim_berners-lee/db/ldpaygo2");
 
// store data on disk	
DataStore dataStore = new RDBMSDataStore(
	new H2DatabaseDriver(Type.Disk, dbpath, false),config);

// create the PSL model
PSLModel model = new PSLModel(this, dataStore);
	
////////////////////////// predicate declaration ////////////////////////

/** OntologyTriples **/
model.add predicate: "OntTriple",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID, ArgumentType.UniqueID];


/** load observed atoms **/
def observedMap = [
	((Predicate)OntTriple):ontTriplesPath];



Partition observedPart = new Partition(100);

PSLUtil.loadFromCSV(dataStore, observedMap, observedPart);

Date stop = new Date();

TimeDuration td = TimeCategory.minus( stop, start );

System.out.println "[info] Loading " + ontTriplesPath + " finished in: " + td;






	

