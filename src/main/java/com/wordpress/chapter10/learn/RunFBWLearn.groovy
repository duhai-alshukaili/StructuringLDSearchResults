/**
 * FileName: Run2LevelWLearn23WithFB.groovy
 * Date    : 28-5-2015
 *
 * @author Duhai Alshukaili
 *
 * A groovy class that runs the PSL model inference. It takes two arguments
 * as command line arguments:
 *
 * arg1: The stratification level (1-8)
 * arg2: The path of the output directory where inference results will be written
 *       to.
 */

package com.wordpress.chapter10.learn;



import com.wordpress.chapter10.infer.external.EqualPrefixImpl
import com.wordpress.chapter10.infer.external.LexicalDateStringSimilarity
import com.wordpress.chapter10.infer.external.LexicalIndividualURISimilarity
import com.wordpress.chapter10.infer.external.LexicalLongStringSimilarity
import com.wordpress.chapter10.infer.external.LexicalNumericStringSimilarity
import com.wordpress.chapter10.infer.external.LexicalShortStringSimilarity
import com.wordpress.chapter10.infer.external.LexicalURISimilarity
import com.wordpress.chapter10.infer.external.LexicalVocabURISimilarity
import com.wordpress.chapter10.infer.external.MetaPredicateImpl;
import com.wordpress.chapter10.infer.external.MetaTypeImpl;
import com.wordpress.chapter10.infer.external.MetaVocabImpl
import com.wordpress.chapter10.infer.external.YagoTypeImpl;
import com.wordpress.chapter10.util.PSLUtil;

import edu.umd.cs.psl.application.inference.*;
//import edu.umd.cs.psl.application.learning.weight.em.HardEM;
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


// intital weights
//def fp = PSLUtil.loadWeightMap("data/weights/f.txt", 18, "f");
def fp = PSLUtil.initializeWeightMap(3.0, 18, "f");

// initialize some setting


String dataroot = args[0];

String rdfInput = dataroot + File.separator + "rdf" + File.separator;
String feedbackInput = dataroot + File.separator + "feedback" + File.separator;
String ontInput = dataroot + File.separator + "ont" + File.separator;
String gtInput = dataroot + File.separator + "gt" + File.separator;
String output = dataroot + File.separator + "output" + File.separator;
//String dbfiles = dataroot + File.separator + "db" + File.separator;
String dbfiles = "/media/black/";
////////////////////////////////////////////////////////////////////////////////

// initialize PSL environment configuration
ConfigManager configManager = ConfigManager.getManager();
ConfigBundle configBundle = configManager.getBundle("ldpaygo2");
configBundle.addProperty("rdbmsdatastore.usestringids", true);


String defaultPath  = System.getProperty("user.dir");

String dbpath       = configBundle.getString("dbpath",
	dbfiles + "ldpaygo2");

DataStore dataStore = new RDBMSDataStore(
		new H2DatabaseDriver(Type.Disk, dbpath, true),configBundle);

////////////////////////////////////////////////////////////////////////////////
// load data
////////////////////////////////////////////////////////////////////////////////
PSLModel model = new PSLModel(this, dataStore);




model.add predicate: "Entity",  types: [ArgumentType.UniqueID];

model.add predicate: "Property",  types: [ArgumentType.UniqueID];

model.add predicate: "EntityType",  types: [ArgumentType.UniqueID];

model.add predicate: "RDFIsInstanceOf",  types: [ArgumentType.UniqueID,ArgumentType.UniqueID];

model.add predicate: "HasType",  types: [ArgumentType.UniqueID,ArgumentType.UniqueID];

model.add predicate: "HasProperty", types: [ArgumentType.UniqueID,ArgumentType.UniqueID];

model.add predicate: "SimProperty", types: [ArgumentType.UniqueID,ArgumentType.UniqueID];

model.add predicate: "SimEntityType",  types: [ArgumentType.UniqueID,ArgumentType.UniqueID];

model.add predicate: "SimEntity",  types: [ArgumentType.UniqueID,ArgumentType.UniqueID];

// EntityTypeFB(T, UID, Term)
model.add predicate: "EntityTypeFB", types: [ArgumentType.UniqueID,ArgumentType.UniqueID,ArgumentType.String];

//HasTypeFB(E, T, UID, Term)
model.add predicate: "HasTypeFB", types: [ArgumentType.UniqueID,ArgumentType.UniqueID,ArgumentType.UniqueID,ArgumentType.String];

// HasPropertyFB(T, P, UID, Term)
model.add predicate: "HasPropertyFB", types: [ArgumentType.UniqueID,ArgumentType.UniqueID,ArgumentType.UniqueID,ArgumentType.String];

//SimPropertyFB(P1, P2, UID, Term)
model.add predicate: "SimPropertyFB", types: [ArgumentType.UniqueID,ArgumentType.UniqueID,ArgumentType.UniqueID,ArgumentType.String];

//SimEntityTypeFB(T1, T2, UID, Term)
model.add predicate: "SimEntityTypeFB", types: [ArgumentType.UniqueID,ArgumentType.UniqueID,ArgumentType.UniqueID,ArgumentType.String];


//SimEntityFB(T1, T2, UID, Term)
model.add predicate: "SimEntityFB", types: [ArgumentType.UniqueID,ArgumentType.UniqueID,ArgumentType.UniqueID,ArgumentType.String];


model.add predicate: "RawType", types: [ArgumentType.UniqueID];

model.add predicate: "RawPredicate", types: [ArgumentType.UniqueID];

model.add predicate: "RawSubject", types: [ArgumentType.UniqueID];

model.add predicate: "RawObject", types: [ArgumentType.UniqueID];


double p = 1.0;

// Feedback Rules
model.add rule: (RDFIsInstanceOf(S,T) & EntityTypeFB(T,UID,"yes")) >> Entity(S), weight: fp["f1"];
model.add rule: (RDFIsInstanceOf(S,T) & EntityTypeFB(T,UID,"no")) >> ~Entity(S), weight: fp["f2"];
model.add rule: (RDFIsInstanceOf(S,T) & HasTypeFB(S,T,UID,"yes")) >> Entity(S), weight: fp["f3"];
model.add rule: (RDFIsInstanceOf(S,T) & HasTypeFB(S,T,UID,"no")) >> ~Entity(S), weight: fp["f4"];

model.add rule: (EntityTypeFB(T,UID,"yes")) >> EntityType(T), weight: fp["f5"];
model.add rule: (EntityTypeFB(T,UID,"no")) >> ~EntityType(T), weight: fp["f6"];

model.add rule: (HasTypeFB(S,T,UID, "yes")) >> HasType(S,T), weight: fp["f7"];
model.add rule: (HasTypeFB(S,T,UID, "no")) >> ~HasType(S,T), weight: fp["f8"];

model.add rule: (HasPropertyFB(T,P,UID,"yes")) >> Property(P), weight: fp["f9"];
model.add rule: (HasPropertyFB(T,P,UID,"no")) >> ~Property(P), weight: fp["f10"];

model.add rule: (HasPropertyFB(T,P,UID,"yes")) >> HasProperty(T,P), weight: fp["f11"];
model.add rule: (HasPropertyFB(T,P,UID,"no")) >> ~HasProperty(T,P), weight: fp["f12"];

model.add rule: (SimEntityTypeFB(T1,T2,UID,"yes")) >> SimEntityType(T1,T2), weight: fp["f13"];
model.add rule: (SimEntityTypeFB(T1,T2,UID,"no")) >> ~SimEntityType(T1,T2), weight: fp["f14"];

model.add rule: (SimPropertyFB(P1,P2,UID,"yes")) >> SimProperty(P1,P2), weight: fp["f15"];
model.add rule: (SimPropertyFB(P1,P2,UID,"no")) >> ~SimProperty(P1,P2), weight: fp["f16"];

model.add rule: (SimEntityFB(E1,E2,UID,"yes")) >> SimEntity(E1,E2), weight: fp["f17"];
model.add rule: (SimEntityFB(E1,E2,UID,"no")) >> ~SimEntity(E1,E2), weight: fp["f18"];

//model.add rule: (Feedback2(T1,T2,U, "yes" ,"type_disjointness")) >> ~SimEntityType(T1,T2), weight: w1;
//model.add rule: (HasType(E1,T1) & HasType(E2,T2) & Feedback2(T1,T2,U, "yes" ,"type_disjointness")) >> ~SimEntity(E1,E2), weight: w1;

model.add rule : ~Property(E), weight: p;
model.add rule : ~Entity(E), weight: p;
model.add rule : ~EntityType(ET), weight: p;
model.add rule : ~HasType(S,T), weight: p;
model.add rule : ~HasProperty(S,T), weight: p;
model.add rule : ~SimEntity(E1,E2), weight: p;
model.add rule : ~SimEntityType(ET1,ET2), weight: p;
model.add rule : ~SimProperty(P1,P2), weight: p;

////////////////////////////////////////////////////////////////////////////////

// load the data

Partition observedPart = new Partition(100);
Partition gtPart = new Partition(101);
Partition inferPart = new Partition(102);

def observedMap = [
	((Predicate)EntityTypeFB):feedbackInput+"/EntityTypeFB.csv",
	((Predicate)HasTypeFB):feedbackInput+"/HasTypeFB.csv",
	((Predicate)HasPropertyFB):feedbackInput+"/HasPropertyFB.csv",
	((Predicate)SimPropertyFB):feedbackInput+"/SimPropertyFB.csv",
	((Predicate)SimEntityTypeFB):feedbackInput+"/SimEntityTypeFB.csv",
	((Predicate)SimEntityFB):feedbackInput+"/SimEntityFB.csv",
		// raw data
	((Predicate)RawType):rdfInput+"/rdftype.csv",
	((Predicate)RawSubject):rdfInput+"/subject.csv",
	((Predicate)RawPredicate):rdfInput+"/predicate.csv",
	((Predicate)RawObject):rdfInput+"/object.csv"];


def observedMapWithTruth =  [
	((Predicate)model.getPredicate("RDFIsInstanceOf")):[
		output+"/RDFIsInstanceOf.csv"
	]]

PSLUtil.loadFromCSV(dataStore, observedMap, observedPart);
PSLUtil.loadFromCSVWithTruthValue(dataStore, observedMapWithTruth, observedPart);

/*
def predicateFileMap = [((Predicate)EntityTypeFB):output+"/EntityTypeFB.csv",
	((Predicate)HasTypeFB):output+"/HasTypeFB.csv",
	((Predicate)HasPropertyFB):output+"/HasPropertyFB.csv",
	((Predicate)SimPropertyFB):output+"/SimPropertyFB.csv",
	((Predicate)SimEntityTypeFB):output+"/SimEntityTypeFB.csv",
	((Predicate)SimEntityFB):output+"/SimEntityFB.csv"];

//printResults(dataStore, observedPart, predicateFileMap);
*/



def closedWorldPredicateSet = [
	EntityTypeFB,
	HasTypeFB,
	HasPropertyFB,
	SimPropertyFB,
	SimEntityTypeFB,
	SimEntityFB,
	RDFIsInstanceOf] as Set;
// a set of target query predicates
def gtPredSet = [
	SimEntity,
	SimEntityType,
	SimProperty,
	Entity,
	HasType,
	EntityType,
	HasProperty,
	Property] as Set;

// load the ground truth
def gtPredMap =  [
	((Predicate)model.getPredicate("Entity")):[gtInput+"/GT_Entity.csv"],
	((Predicate)model.getPredicate("Property")):[gtInput+"/GT_Property.csv"],
	((Predicate)model.getPredicate("EntityType")):[gtInput+"/GT_EntityType.csv"],
	((Predicate)model.getPredicate("HasType")):[gtInput+"/GT_HasType.csv"],
	((Predicate)model.getPredicate("HasProperty")):[gtInput+"/GT_HasProperty.csv"],
	((Predicate)model.getPredicate("SimProperty")):[gtInput+"/GT_SimProperty.csv"],
	((Predicate)model.getPredicate("SimEntity")):[gtInput+"/GT_SimEntity.csv"],
	((Predicate)model.getPredicate("SimEntityType")):[gtInput+"/GT_SimEntityType.csv"]];

PSLUtil.loadFromCSVWithTruthValue(dataStore, gtPredMap, gtPart);


// the training database, write inference results here
Database trainDB = dataStore.getDatabase(inferPart,
		closedWorldPredicateSet, observedPart);
	
// query raw ground terms for full weight learning db population
System.out.println("Querying ground terms");
	
Set<GroundTerm> subjectSet = PSLUtil.getGroundTermSet(trainDB,
		(Predicate)model.getPredicate("RawSubject"));

Set<GroundTerm> predicateSet = PSLUtil.getGroundTermSet(trainDB,
		(Predicate)model.getPredicate("RawPredicate"));

//Set<GroundTerm>  objectSet = PSLUtil.getGroundTermSet(trainDB,
//		(Predicate)model.getPredicate("RawObject"));

Set<GroundTerm> typeSet = PSLUtil.getGroundTermSet(trainDB,
		(Predicate)model.getPredicate("RawType"));
	
////////////////////////////////////////////////////////////////////
System.out.println("[info]: Grounding Candidates of Entity");
PSLUtil.populateSingle(trainDB
		,subjectSet
		,(Predicate)model.getPredicate("Entity"));

System.out.println("[info]: Grounding Candidates of EntityType");
PSLUtil.populateSingle(trainDB
		,typeSet
		,((Predicate)model.getPredicate("EntityType")));

System.out.println("[info]: Grounding Candidates of Property");
PSLUtil.populateSingle(trainDB
		,predicateSet
		,((Predicate)model.getPredicate("Property")));


///////////////////////////////////////////////////////////
System.out.println("[info]: Grounding Candidates of HasType");
PSLUtil.populatePair(trainDB
		,subjectSet
		,typeSet
		,((Predicate)model.getPredicate("HasType")));

System.out.println("[info]: Grounding Candidates of HasProperty");
PSLUtil.populatePair(trainDB
		,typeSet
		,predicateSet
		,((Predicate)model.getPredicate("HasProperty")));
	
////////////////////////////////////////////////////////////////////
System.out.println("[info]: Grounding Candidates of SimProperty");
PSLUtil.populateSimPair(trainDB
		,predicateSet
		,((Predicate)model.getPredicate("SimProperty")));
	
////////////////////////////////////////////////////////////////////
System.out.println("[info]: Grounding Candidates of SimEntityType");
	PSLUtil.populateSimPair(trainDB
			,typeSet
			,((Predicate)model.getPredicate("SimEntityType")));

///////////////////////////////////////////////////////////////////	
System.out.println("[info]: Grounding Candidates of SimEntity");
		PSLUtil.populateSimPair(trainDB
				,subjectSet
				,((Predicate)model.getPredicate("SimEntity")));
// the ground truth database
Database truthDB = dataStore.getDatabase(gtPart, gtPredSet);


//HardEM weightLearning = new HardEM(model, trainDB, truthDB, configBundle);
MaxLikelihoodMPE weightLearning = new MaxLikelihoodMPE(model, trainDB, truthDB, configBundle);
weightLearning.learn();
weightLearning.close();

println model;

logModel(output, model, 1);

////////////////////////////////////////////////////////////////////////////////



/**
 * 
 * @param level
 * @param start
 * @param path
 * @return
 */
def logStart(int level, Date start, String path) {

	def writer = new File(path + "/learn_log.txt").newWriter("UTF-8", true);
	writer.write("" + start + "\t\tLevel" + level + " running weight learning\n");
	writer.close();

}

/**
 * 
 * @param level
 * @param stop
 * @param td
 * @param path
 * @return
 */
def logEnd(int level, Date stop, TimeDuration td, String path) {
	def writer = new File(path + "/learn_log.txt").newWriter("UTF-8", true);
	writer.write("" + stop + "\t\tLevel" + level + "  weight learning finished in: " + td + "\n");
	writer.close();
}


/**
 * 
 * @param dataroot
 * @param model
 * @param level
 * @return
 */
def logModel(String dataroot, PSLModel model, int level) {
	def writer = new File(dataroot + "/FBModel.txt").newWriter("UTF-8", true);
	writer.write("Model of level: " + level + "\n");
	writer.write(model.toString() + "\n");
	writer.close();
}

/**
 * 
 * @param gtPath
 * @param model
 * @param predicates
 * @return
 */
def predicateMap(String gtPath, PSLModel model, List predicates) {

	def dataSets = PSLUtil.dataSetList();

	def gtMap = [:];

	predicates.each { predicateName ->

		def fileList = [];

		dataSets.each { dataSetName ->

			StringBuilder builder = new StringBuilder();
			builder.append(gtPath)
					.append("/").append(dataSetName)
					.append("/GT_").append(predicateName)
					.append(".csv");

			fileList.add(builder.toString());
		}

		gtMap.put((Predicate)model.getPredicate(predicateName), fileList);
	}

	return gtMap;
}



////////////////////////////////////////////////////////////////////////////////

/**
 *
 * @param dataStore
 * @param partition
 * @param predicateFileMap
 */
def printResults(DataStore dataStore, Partition partition, Map predicateFileMap) {
	predicateFileMap.each {predicate, file ->
		PSLUtil.printCSVResults(dataStore, partition, predicate, file, false);
	}
}
