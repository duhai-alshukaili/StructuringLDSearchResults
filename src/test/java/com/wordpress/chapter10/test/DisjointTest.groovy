package com.wordpress.chapter10.test


import java.text.DecimalFormat;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein

import com.wordpress.chapter10.util.PSLUtil;
import com.wordpress.chapter10.infer.external.SimilarLabelsImpl;
import edu.umd.cs.psl.groovy.*;
import edu.umd.cs.psl.ui.functions.textsimilarity.*;
import edu.umd.cs.psl.ui.loading.InserterUtils;
import edu.umd.cs.psl.util.database.Queries;
import edu.umd.cs.psl.model.argument.ArgumentType;
import edu.umd.cs.psl.model.argument.GroundTerm;
import edu.umd.cs.psl.model.predicate.Predicate;
import edu.umd.cs.psl.model.argument.type.*;
import edu.umd.cs.psl.model.atom.GroundAtom;
import edu.umd.cs.psl.model.atom.RandomVariableAtom;
import edu.umd.cs.psl.model.function.ExternalFunction;
import edu.umd.cs.psl.model.predicate.type.*;
import edu.umd.cs.psl.application.inference.LazyMPEInference
import edu.umd.cs.psl.application.inference.MPEInference;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.MaxLikelihoodMPE;
import edu.umd.cs.psl.config.*;
import edu.umd.cs.psl.database.DataStore;
import edu.umd.cs.psl.database.Database;
import edu.umd.cs.psl.database.Partition;
import edu.umd.cs.psl.database.ReadOnlyDatabase;
import edu.umd.cs.psl.database.rdbms.RDBMSDataStore;
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver;
import edu.umd.cs.psl.database.rdbms.driver.H2DatabaseDriver.Type;

ConfigManager cm = ConfigManager.getManager()
ConfigBundle config = cm.getBundle("disjointTest")
def defaultPath = System.getProperty("java.io.tmpdir")
String dbpath = config.getString("dbpath", defaultPath 
	+ File.separator + "disjoint-test")
DataStore dataStore = new RDBMSDataStore(new H2DatabaseDriver(Type.Disk, dbpath, 
	true), config)
PSLModel m = new PSLModel(this, dataStore);

////////////////////////////////////////////////////////////////////////////

m.add predicate: "Entity",  types: [ArgumentType.UniqueID];
m.add predicate: "RDFSubject",  types: [ArgumentType.UniqueID];
m.add predicate: "Label",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID];
m.add predicate: "HasType", types: [ArgumentType.UniqueID, ArgumentType.UniqueID];
m.add predicate: "DisjointType", types: [ArgumentType.UniqueID, ArgumentType.UniqueID];
m.add predicate: "SimEntity",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID];
m.add predicate: "RDFIsInstanceOf",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID];
m.add predicate: "SimEntityFB",  types: [ArgumentType.UniqueID, ArgumentType.UniqueID, ArgumentType.String];

m.add function: "SimilarLabel", implementation: new SimilarLabelsImpl();

m.add rule: (Entity(A) & Entity(B) & Label(A,L1) & Label(B,L2) & SimilarLabel(L1,L2) & (A-B)) >> SimEntity(A,B), weight: 1, squared: false;
m.add rule: (RDFSubject(S) & RDFIsInstanceOf(S,T)) >> Entity(S), weight: 10, squared: false;
m.add rule: (SimEntityFB(A,B,"NO")) >> ~SimEntity(A,B), weight: 1, squared: false;
//m.add rule: (Entity(A) & Entity(B) & ~SimEntity(A,B)) >>  SimEntityFB(A,B,"NO"), weight: 2;
m.add rule: (Entity(A) & Entity(B) & HasType(A,T1) & HasType(B,T2) & DisjointType(T1,T2) & (A-B)) >> ~SimEntity(A,B), constraint: true;
m.add rule: ~SimEntity(A,B), weight: 2, squared: false;
m.add PredicateConstraint.Symmetric, on: SimEntity;

println m;

String gtPath = "data/disjointTest";

def gtPredMap = [
	((Predicate)m.getPredicate("SimEntityFB")):[gtPath+"/SimEntityFB.csv"],
	((Predicate)m.getPredicate("Entity")):[gtPath+"/Entity.csv"],
	((Predicate)m.getPredicate("HasType")):[gtPath+"/HasType.csv"],
	((Predicate)m.getPredicate("Label")):[gtPath+"/Label.csv"],
	((Predicate)m.getPredicate("DisjointType")):[gtPath+"/DisjointType.csv"]];

Partition dataPart = new Partition(100);
Partition inferPart = new Partition(200);


PSLUtil.loadFromCSVWithTruthValue(dataStore, gtPredMap, dataPart);


Database inferredDB = dataStore.getDatabase(inferPart,
		[Entity, SimEntityFB, HasType, Label, EntityType] as Set, dataPart);

LazyMPEInference inference = new LazyMPEInference(m, inferredDB, config);
inference.mpeInference();
inference.close();
inferredDB.close();

def predicateFileMap = [
	((Predicate)m.getPredicate("SimEntity")):gtPath +"/SimEntity.csv"];

PSLUtil.printResults(dataStore, inferPart, predicateFileMap);

PSLUtil.printCSVResults(dataStore 
	                   ,inferPart
					   ,(Predicate)m.getPredicate("SimEntity") 
                       ,System.out 
					   ,false);

