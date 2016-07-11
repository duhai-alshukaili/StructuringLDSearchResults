/**
 * FileName: RunInfer.groovy
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



import com.hp.hpl.jena.enhanced.Implementation;
import com.wordpress.chapter10.infer.external.EqualPrefixImpl
import com.wordpress.chapter10.infer.external.IsLabelImpl
import com.wordpress.chapter10.infer.external.IsNameImpl
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
import com.wordpress.chapter10.infer.external.SimilarLabelsImpl
import com.wordpress.chapter10.infer.external.SimilarLiteralsImpl
import com.wordpress.chapter10.infer.external.SimilarLocalNamesImpl
import com.wordpress.chapter10.infer.external.SimilarNamesImpl
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


// initialize some setting
int level = Integer.parseInt(args[0]);

String dataroot = args[1];

String rdfInput = dataroot + File.separator + "rdf" + File.separator;
String feedbackInput = dataroot + File.separator + "feedback" + File.separator;
String ontInput = dataroot + File.separator + "ont" + File.separator;
String gtInput = dataroot + File.separator + "gt" + File.separator;
String output = dataroot + File.separator + "output" + File.separator;
////////////////////////////////////////////////////////////////////////////////

// initialize PSL environment configuration
ConfigManager configManager = ConfigManager.getManager();
ConfigBundle configBundle = configManager.getBundle("ldpaygo2");

String defaultPath  = System.getProperty("user.dir");

String dbpath       = configBundle.getString("dbpath",
		defaultPath + File.separator + "ldpaygo2");

// clearDB is false so that we don't delete previous data
DataStore dataStore = new RDBMSDataStore(
		new H2DatabaseDriver(Type.Disk, dbpath, false),configBundle);

////////////////////////////////////////////////////////////////////////////////

PSLModel model;


//def weightMap = PSLUtil.initializeWeightMap(2.0, 25, "w");
//def priorMap = PSLUtil.initializeWeightMap(0.0, 16, "p");

def weightMap = PSLUtil.loadWeightMap("data/weights/w.txt", 27, "w");
def priorMap = PSLUtil.loadWeightMap("data/weights/p.txt", 16, "p");


switch(level) {

	case 0:
	// constraint inference
		model = level0(dataStore);
		println model;

		execusteInferLevel0(dataStore, model, configBundle, output);
		break;

	case 1:
		model = level1(dataStore, weightMap, priorMap);

		execusteWLearnLevel1(dataStore, model, configBundle, output, gtInput);

		break;

	case 2:
		model = level2(dataStore, weightMap, priorMap);
		
		execusteWLearnLevel2(dataStore, model, configBundle, output,gtInput);

		break;
	

	default:
		println "Invalid Inference level";
}





///////////////////////// level 0 model & inference /////////////////////////

/**
 * Constraints Model
 */
def level0(DataStore data) {
	PSLModel m = new PSLModel(this, data);
	
		//System.out.println "[info] \t\tDECLARING PREDICATES...";
		
		m.add predicate: "ShortStringLit",  types: [ArgumentType.UniqueID];
		
		m.add predicate: "DateLit",  types: [ArgumentType.UniqueID];
		
		m.add predicate: "NumericLit",  types: [ArgumentType.UniqueID];
		
		m.add predicate: "LongStringLit",  types: [ArgumentType.UniqueID];
	
		/** RDF constructs Inferred by constraints **/
		m.add predicate: "RDFSubject",  types: [ArgumentType.UniqueID];
	
		m.add predicate: "RDFPredicate",  types: [ArgumentType.UniqueID];
	
		m.add predicate: "RDFType",  types: [ArgumentType.UniqueID];
	
		m.add predicate: "RDFSubjPredicate", types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID];
	
		m.add predicate: "RDFPredicateObj",  types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID];
	
		m.add predicate: "RDFIsInstanceOf",  types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID];
	
		m.add predicate: "SimLit", types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID];
		
		
		m.add predicate: "SimURI", types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID];
		
		m.add predicate: "Name", types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID
		];
	
		m.add predicate: "Label", types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID
		];
		
		m.add predicate: "LnkPropertyValue", types: [ArgumentType.UniqueID];
	
	
		// external function
		m.add function: "fnShortLitSim"   , implementation: new LexicalShortStringSimilarity();
		m.add function: "fnLongLitSim"    , implementation: new LexicalLongStringSimilarity();
		m.add function: "fnDateLitSim"    , implementation: new LexicalDateStringSimilarity();
		m.add function: "fnNumericLitSim" , implementation: new LexicalNumericStringSimilarity();
		m.add function: "fnIndvURISim"    , implementation: new LexicalIndividualURISimilarity();
		m.add function: "fnPropURISim"    , implementation: new LexicalVocabURISimilarity();
		m.add function: "fnTypeURISim"    , implementation: new LexicalVocabURISimilarity();
		m.add function: "fnYagoType"      , implementation: new YagoTypeImpl();
		m.add function: "fnMetaVocab" , implementation: new MetaVocabImpl();
		m.add function: "IsLabel"      , implementation: new IsLabelImpl();
		m.add function: "IsName" , implementation: new IsNameImpl();
		
		////////////////////////// rule declaration ////////////////////////
		// System.out.println "[info] \t\tDECLARING RULES...";
	
		// rdf:type construct and freebase alternative
		def rdfTypeID = data.getUniqueID("rdf:type");
		// def freebaseTypeID = data.getUniqueID("freebase:type.object.type");
	
		/** constraints **/
		m.add rule: (Triple1(S,P,O)) >> RDFSubject(S), constraint: true;
	
		m.add rule: (Triple2(S,P,O)) >> RDFSubject(S), constraint: true;
	
		m.add rule: (Triple1(S,P,O)) >> RDFPredicate(P), constraint: true;
	
		m.add rule: (Triple2(S,P,O)) >> RDFPredicate(P), constraint: true;
	
		m.add rule: (Triple1(S,rdfTypeID,O) ) >> RDFType(O), constraint: true;
	
		m.add rule: (Triple1(S,rdfTypeID,O) ) >> RDFIsInstanceOf(S,O), constraint: true;
	
		//m.add rule: (Triple1(S,freebaseTypeID,O) ) >> RDFType(O), constraint: true;
	
		//m.add rule: (Triple1(S,freebaseTypeID,O) ) >> RDFIsInstanceOf(S,O), constraint: true;
	
		m.add rule: (Triple1(S,P,O)) >> RDFSubjPredicate(S,P), constraint: true;
	
		m.add rule: (Triple2(S,P,O)) >> RDFSubjPredicate(S,P), constraint: true;
		
		m.add rule: (Triple1(S,P,O)) >> LnkPropertyValue(O), constraint: true;
		
		m.add rule: (Triple1(S,P,O) & IsLabel(P)) >> Label(S,O), constraint: true;
		
		m.add rule: (Triple2(S,P,O) & IsLabel(P)) >> Label(S,O), constraint: true;
		
		m.add rule: (Triple1(S,P,O) & IsName(P)) >> Name(S,O), constraint: true;
		
		m.add rule: (Triple2(S,P,O) & IsName(P)) >> Name(S,O), constraint: true;
		//m.add rule: (Triple1(S,P,O)) >> RDFPredicateObj(P,O), constraint: true;
	
		//m.add rule: (Triple2(S,P,O)) >> RDFPredicateObj(P,O), constraint: true;
		
		
		m.add rule: (ShortStringLit(L1) & ShortStringLit(L2) &
			fnShortLitSim(L1,L2)) >> SimLit(L1,L2), constraint: true;
		
		m.add rule: (LongStringLit(L1) & LongStringLit(L2) &
			fnLongLitSim(L1,L2)) >> SimLit(L1,L2), constraint: true;
		
		m.add rule: (DateLit(L1) & DateLit(L2) &
			fnDateLitSim(L1,L2)) >> SimLit(L1,L2), constraint: true;
		
		m.add rule: (NumericLit(L1) & NumericLit(L2) &
			fnNumericLitSim(L1,L2)) >> SimLit(L1,L2), constraint: true;
		
		m.add rule: (LnkPropertyValue(L1) & LnkPropertyValue(L2)
			& fnIndvURISim(L1,L2)) >> SimURI(L1,L2), constraint: true;
		
		m.add rule: (
			RDFType(L1) & RDFType(L2) &
			~fnYagoType(L1) & ~fnYagoType(L2) & ~fnMetaVocab(L1) & ~fnMetaVocab(L2) &
			fnTypeURISim(L1,L2)) >> SimURI(L1,L2), constraint: true;
		
		m.add rule: (
			RDFPredicate(L1) & RDFPredicate(L2) &
			~fnMetaVocab(L1) & ~fnMetaVocab(L2) &
			fnPropURISim(L1,L2)) >> SimURI(L1,L2), constraint: true;
		
		
		
		m.add PredicateConstraint.Symmetric, on: SimLit;
		m.add PredicateConstraint.Symmetric, on: SimURI;
		
		m.add rule: (RDFSubject(S)) >> SimURI(S,S), constraint: true;
	
		return m;
}


def execusteInferLevel0(DataStore dataStore, PSLModel model,
		ConfigBundle configBundle, String dataroot) {



	// observed predicates
	def closedWorldPredicateSet = [Triple1, Triple2, 
		ShortStringLit, DateLit, NumericLit, LongStringLit] as Set;

	// declare the partitions
	Partition observedPart = new Partition(100);
	Partition constraintInferPart = new Partition(102);

	System.out.println "[info] Level0 running inference...";


	Date start = new Date();

	logStart(0, start, dataroot);

	Database inferredDB = dataStore.getDatabase(constraintInferPart,
			closedWorldPredicateSet,
			observedPart);

	LazyMPEInference inference = new LazyMPEInference(model, inferredDB, configBundle);
	inference.mpeInference();
	inference.close();
	inferredDB.close();

	Date stop = new Date();

	TimeDuration td = TimeCategory.minus( stop, start );

	System.out.println("[info] inference finished in " + td);

	logEnd(0, stop, td, dataroot);



}

///////////////////////////////////////////////////////////////////////////////

def level1(DataStore data, def w, def p) {

	PSLModel model = new PSLModel(this, data);
	
		model.add predicate: "Entity",  types: [ArgumentType.UniqueID];
	
		model.add predicate: "EntityType",  types: [ArgumentType.UniqueID];
	
		model.add predicate: "Property",  types: [ArgumentType.UniqueID];
	
		model.add predicate: "PropertyValue",  types: [ArgumentType.UniqueID];
	
		model.add predicate: "HasPropertyValue",  types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID
		];
	
		model.add predicate: "HasShortLitPropertyValue",  types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID
		];
	
		model.add predicate: "HasLongLitPropertyValue",  types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID
		];
	
		model.add predicate: "HasDateLitPropertyValue",  types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID
		];
	
		model.add predicate: "HasNumericLitPropertyValue",  types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID
		];
	
		model.add predicate: "HasDomain",  types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID
		];
	
		model.add predicate: "HasType",  types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID
		];
	
		model.add predicate: "HasProperty", types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID
		];
	
		/** similarity predicates **/
		model.add predicate: "SimURI", types: [
			ArgumentType.UniqueID,
			ArgumentType.UniqueID
		];
	
		/** External functions **/
		model.add function: "fnYagoType" , implementation: new YagoTypeImpl();
		model.add function: "fnMetaVocab" , implementation: new MetaVocabImpl();
		model.add function: "fnEqPrefix" , implementation: new EqualPrefixImpl();
	
		/** Set similarity **/
		model.add setcomparison: "SimURISet" , using: SetComparison.CrossEquality, on : SimURI;
	
	
		////////////////////////// rule declaration ////////////////////////
	
		/** ER Inference Rules **/
	
		// model.add rule: (RDFSubject(S) & ~RDFType(S) & ~RDFPredicate(S)) >> Entity(S), weight: w1;
	
		//model.add rule: (RDFSubject(S1) & RDFSubject(S2) & RDFIsInstanceOf(S1,T) & RDFIsInstanceOf(S2,T) &
		//	SimURISet ({S1.RDFSubjPredicate} , {S2.RDFSubjPredicate}))  >> Entity(S1), weight: w1;
	
		//model.add rule: (RDFSubject(S1) & RDFSubject(S2) & RDFIsInstanceOf(S1,T) & RDFIsInstanceOf(S2,T) &
		//	 SimURISet ({S1.RDFIsInstanceOf} , {S2.RDFIsInstanceOf}))  >> Entity(S1), weight: w1;
	
		/** Entity **/
		model.add rule: (RDFSubject(S) & RDFIsInstanceOf(S,T)) >> Entity(S), weight: w["w1"];
		model.add rule: (RDFSubject(S) & RDFType(S)) >> ~Entity(S), constraint: true;
		model.add rule: (RDFSubject(S) & RDFPredicate(S)) >> ~Entity(S), constraint: true;
		
		// property
		model.add rule: (RDFSubjPredicate(S,P) & RDFIsInstanceOf(S,T)) >> Property(P), weight: w["w2"];
		model.add rule: (RDFPredicate(P) & fnMetaVocab(P)) >> ~Property(P), constraint: true;
		
		// EntityType
		model.add rule: (RDFIsInstanceOf(S,T) & ~RDFType(S) & ~RDFPredicate(S)) >> EntityType(T), weight: w["w3"];
		model.add rule: (RDFType(T) & fnMetaVocab(T)) >> ~EntityType(T), constraint: true;
		model.add rule: (RDFType(T) & fnYagoType(T)) >> ~EntityType(T), constraint: true;
		
		// PropertyValue
		model.add rule: (Triple1(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T)) >> PropertyValue(O), weight: w["w4"];
		model.add rule: (Triple2(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T)) >> PropertyValue(O), weight: w["w5"];
	
		// HasPropertyValue
		model.add rule: (Triple1(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T)) >> HasPropertyValue(S,O), weight: w["w6"];
		model.add rule: (Triple2(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T)) >> HasPropertyValue(S,O), weight: w["w7"];
		
		model.add rule: (Triple2(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T) & ShortStringLit(O)) >> HasShortLitPropertyValue(S,O), weight: w["w8"];
		model.add rule: (Triple2(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T) & LongStringLit(O))  >> HasLongLitPropertyValue(S,O), weight: w["w9"];
		model.add rule: (Triple2(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T) & DateLit(O)) >> HasDateLitPropertyValue(S,O), weight: w["w10"];
		model.add rule: (Triple2(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T) & NumericLit(O)) >> HasNumericLitPropertyValue(S,O), weight: w["w11"];
	
		// HasDomain
		model.add rule: (Triple1(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T)) >> HasDomain(O,P), weight: w["w12"];
		model.add rule: (Triple2(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T)) >> HasDomain(O,P), weight: w["w13"];
	
		// HasType
		//model.add rule: (RDFIsInstanceOf(S,T) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(T) & ~fnYagoType(T)) >> HasType(S,T), weight: w["w14"];
		model.add rule: (EntityType(T) & Entity(S)  & RDFIsInstanceOf(S,T)) >> HasType(S,T), weight: w["w14"];
		model.add rule: (RDFIsInstanceOf(S,T) & fnMetaVocab(T)) >> ~HasType(S,T), constraint: true;
		model.add rule: (RDFIsInstanceOf(S,T) & fnYagoType(T)) >> ~HasType(S,T), constraint: true;
		
		// HasProperty
		model.add rule: (RDFPredicate(P) & RDFType(T) & /* fnEqPrefix(T,P) & */
		~fnMetaVocab(T) & ~fnYagoType(T) & ~fnMetaVocab(P) &
		SimURISet ({T.RDFIsInstanceOf(inv)} , {P.RDFSubjPredicate(inv)})) >> (HasProperty(T,P)), weight: w["w15"];
	
	
		// similarity constraints
		//model.add rule : (Entity(E1) & Entity(E2) & fnLexURISim(E1,E2)) >> SimURI(E1,E2), constraint: true;
		//model.add rule : (Property(E1) & Property(E2) & fnLexURISim(E1,E2)) >> SimURI(E1,E2), constraint: true;
		//model.add rule : (EntityType(E1) & EntityType(E2) & fnLexURISim(E1,E2)) >> SimURI(E1,E2), constraint: true;
	
		// priors
		
		
		 model.add rule : ~Entity(E), weight: p["p1"];
		 model.add rule : ~EntityType(ET), weight: p["p2"];
		 model.add rule : ~Property(P), weight: p["p3"];
		 model.add rule : ~PropertyValue(O), weight: p["p4"];
		 model.add rule : ~HasPropertyValue(S,O), weight: p["p5"];
		 model.add rule : ~HasDomain(O,P), weight: p["p6"];
		 model.add rule : ~HasType(S,T), weight: p["p7"];
		 model.add rule : ~HasProperty(S,T), weight: p["p8"];
		 
		 model.add rule : ~HasShortLitPropertyValue(S,O), weight: p["p9"];
		 model.add rule : ~HasLongLitPropertyValue(S,O), weight: p["p10"];
		 model.add rule : ~HasDateLitPropertyValue(S,O), weight: p["p11"];
		 model.add rule : ~HasNumericLitPropertyValue(S,O), weight: p["p12"];
		 
		return model;

}

def execusteWLearnLevel1(DataStore dataStore, PSLModel model,
		ConfigBundle configBundle, String dataroot, String gtPath) {

	// observed predicates
	def closedWorldPredicateSet = [Triple1, Triple2, 
		ShortStringLit, DateLit, NumericLit, LongStringLit,
		SimURI,
		SimLit, LnkPropertyValue,
		RDFSubject,
		RDFPredicate,
		RDFType,
		RDFSubjPredicate,
		RDFIsInstanceOf, Name, Label] as Set;

	// declare the partitions
	Partition observedPart = new Partition(100);
	Partition gtPart = new Partition(101);
	Partition constraintInferPart = new Partition(102);
	Partition level1InferPart = new Partition(301);
	
	// a set of target query predicates
	def gtPredSet = [Entity,Property, EntityType, HasProperty,
			HasShortLitPropertyValue, HasLongLitPropertyValue,
			HasDateLitPropertyValue, HasNumericLitPropertyValue,
				 (Predicate)model.getPredicate("PropertyValue"),
				 HasPropertyValue, HasDomain, HasType] as Set;
			 
	 def gtPredMap = [
		 ((Predicate)model.getPredicate("Entity")):[gtPath+"/GT_Entity.csv"],
		 ((Predicate)model.getPredicate("HasProperty")):[gtPath+"/GT_HasProperty.csv"],
		 ((Predicate)model.getPredicate("Property")):[gtPath+"/GT_Property.csv"],
		 ((Predicate)model.getPredicate("EntityType")):[gtPath+"/GT_EntityType.csv"],
		 ((Predicate)model.getPredicate("PropertyValue")):[gtPath+"/GT_PropertyValue.csv"],
		 ((Predicate)model.getPredicate("HasPropertyValue")):[gtPath+"/GT_HasPropertyValue.csv"],
		 ((Predicate)model.getPredicate("HasShortLitPropertyValue")):[gtPath+"/GT_HasShortLitPropertyValue.csv"],
		 ((Predicate)model.getPredicate("HasLongLitPropertyValue")):[gtPath+"/GT_HasLongLitPropertyValue.csv"],
		 ((Predicate)model.getPredicate("HasDateLitPropertyValue")):[gtPath+"/GT_HasDateLitPropertyValue.csv"],
		 ((Predicate)model.getPredicate("HasNumericLitPropertyValue")):[gtPath+"/GT_HasNumericLitPropertyValue.csv"],
		 ((Predicate)model.getPredicate("HasDomain")):[gtPath+"/GT_HasDomain.csv"],
		 ((Predicate)model.getPredicate("HasType")):[gtPath+"/GT_HasType.csv"]];
			 
	PSLUtil.loadFromCSVWithTruthValue(dataStore, gtPredMap, gtPart);

	
	
	System.out.println "[info] running level1 weight learning...";

	Date start = new Date();

	logStart(1, start, dataroot);
	
	Database trainDB = dataStore.getDatabase(level1InferPart,
		closedWorldPredicateSet, observedPart,constraintInferPart);
	
	Database truthDB = dataStore.getDatabase(gtPart, gtPredSet);

	LazyMaxLikelihoodMPE weightLearning = new LazyMaxLikelihoodMPE(model, trainDB, truthDB, configBundle);
	weightLearning.learn();
	weightLearning.close();

	
	Date stop = new Date();

	TimeDuration td = TimeCategory.minus( stop, start );

	System.out.println("[info] level1 weight learning finished in " + td);

	logEnd(1, stop, td, dataroot);

	// Print the learned model
	println model;
	
	logModel(dataroot, model, 1);
	

}



///////////////////////////////////////////////////////////////////////////////

def level2(DataStore data, def w, def p) {
	PSLModel model = new PSLModel(this, data);
	
	
	/** observed data **/
	model.add predicate: "SimURI",  types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	/** Similarity Predicates **/
	model.add predicate: "SimPropertyValue", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];
	model.add predicate: "SimProperty", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];
	model.add predicate: "SimEntityType",  types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];
	model.add predicate: "SimEntity",  types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	model.add predicate: "Name", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	model.add predicate: "Label", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	model.add predicate: "InBlock", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	model.add predicate: "CandHasType", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	model.add predicate: "CandType", types: [ArgumentType.UniqueID];

	
		/** set similarity **/
		model.add setcomparison: "SimURISet" , using: SetComparison.CrossEquality, on : SimURI;
		model.add setcomparison: "SimValueSet" , using: SetComparison.CrossEquality, on : SimPropertyValue;
		//model.add setcomparison: "SimValueSet" , using: SetComparison.CrossEquality, on : LexPropertyValueSim;
		model.add setcomparison: "SimPropertySet" , using: SetComparison.CrossEquality, on : SimProperty;
		model.add setcomparison: "SimTypeSet" , using: SetComparison.CrossEquality, on : SimEntityType;
	
		/** External functions **/
		model.add function: "fnYagoType" , implementation: new YagoTypeImpl();
		model.add function: "fnMetaType" , implementation: new MetaTypeImpl();
		model.add function: "fnMetaPredicate", implementation: new MetaPredicateImpl();
		model.add function: "fnLexURISim" , implementation: new LexicalURISimilarity();
		model.add function: "SimilarLocalNames" , implementation: new SimilarLocalNamesImpl();
		model.add function: "SimilarLabels", implementation: new SimilarLabelsImpl();
		model.add function: "SimilarNames", implementation: new SimilarNamesImpl();
		model.add function: "SimilarLiterals", implementation: new SimilarLiteralsImpl();
		model.add function: "fnMetaVocab" , implementation: new MetaVocabImpl();
	
		// SimPropertyValue
		model.add rule: (PropertyValue(V1) & PropertyValue(V2) & SimURI(V1,V2)) >> SimPropertyValue(V1,V2), weight: w["w16"];
		model.add rule: (PropertyValue(V1) & PropertyValue(V2) & SimLit(V1,V2)) >> SimPropertyValue(V1,V2), weight: w["w17"];
		
		// SimProperty
		// lexical similarity of properties
		model.add rule: (Property(P1) & Property(P2)  & SimURI(P1,P2)) >> SimProperty(P1,P2), weight: w["w18"];
	
		model.add rule: (Property(P1) & Property(P2) &
		SimValueSet({P1.HasDomain(inv)},{P2.HasDomain(inv)})) >> SimProperty(P1,P2), weight: w["w19"];
	
		// SimEntityType
		model.add rule: (EntityType(T1) & EntityType(T2)  & SimURI(T1,T2) ) >> SimEntityType(T1,T2), weight: w["w20"];
	
		model.add rule: (EntityType(T1) & EntityType(T2) & SimPropertySet({T1.HasProperty},{T2.HasProperty})) >> SimEntityType(T1,T2), weight: w["w21"];
	
	
		// SimEntity
		//model.add rule: (Entity(E1) & Entity(E2) &
		//SimValueSet ({E1.HasShortLitPropertyValue} , {E2.HasShortLitPropertyValue})) >> SimEntity(E1,E2), weight: w["w22"];
	
		//model.add rule: (Entity(E1) & Entity(E2) &
		//SimValueSet ({E1.HasLongLitPropertyValue} , {E2.HasLongLitPropertyValue})) >> SimEntity(E1,E2), weight: w["w23"];
	
		//model.add rule: (Entity(E1) & Entity(E2) &
		//SimValueSet ({E1.HasDateLitPropertyValue} , {E2.HasDateLitPropertyValue})) >> SimEntity(E1,E2), weight: w["w24"];
	
	
		//model.add rule: (Entity(E1) & Entity(E2) &
		//SimValueSet ({E1.HasNumericLitPropertyValue} , {E2.HasNumericLitPropertyValue})) >> SimEntity(E1,E2), weight: w["w25"];
	
		model.add rule: (InBlock(E1,E2) & Label(E1, L1) & Label(E2, L2) & SimilarLabels(L1,L2)) >> SimEntity(E1,E2), weight: w["w22"];
		
		model.add rule: (InBlock(E1,E2) & Label(E1, L1) & Label(E2, L2) & ~SimilarLabels(L1,L2)) >> ~SimEntity(E1,E2), weight: w["w23"];
		
		model.add rule: (InBlock(E1,E2) & Name(E1, N1) & Name(E2, N2) & SimilarNames(N1,N2)) >> SimEntity(E1,E2), weight: w["w24"];
		
		model.add rule: (InBlock(E1,E2) & Name(E1, N1) & Name(E2, N2) & ~SimilarNames(N1,N2)) >> ~SimEntity(E1,E2), weight: w["w25"];
		
		model.add rule: (InBlock(E1,E2) & Triple1(E1, P1, O1) & Triple1(E2, P2, O2) & Property(P1) & Property(P2) & SimProperty(P1,P2) & fnLexURISim(O1,O2)) >> SimEntity(E1,E2), weight: w["w26"];
		
		model.add rule: (InBlock(E1,E2) & Triple2(E1, P1, O1) & Triple2(E2, P2, O2) & Property(P1) & Property(P2) & SimProperty(P1,P2) & SimilarLiterals(O1,O2)) >> SimEntity(E1,E2), weight: w["w27"];
		
	    // model.add rule: (Entity(E1) & Entity(E2) & RDFIsInstanceOf(E1,T1) & RDFIsInstanceOf(E2, T2) & SimEntityType(T1,T2)) >> InBlock(E1,E2), constraint: true
		
		model.add rule: (Entity(E1) & RDFIsInstanceOf(E1,T1) & ~fnMetaVocab(T1) & ~fnYagoType(T1)) >> CandHasType(E1,T1), constraint: true
	
		model.add rule: (Entity(E1) & Entity(E2) & CandHasType(E1,T1) & CandHasType(E2, T2) & SimilarLocalNames(T1,T2)) >> InBlock(E1,E2), constraint: true
	
		model.add rule: (Entity(E1) & Entity(E2) & HasType(E1,T1) & HasType(E2, T2) & SimEntityType(T1,T2)) >> InBlock(E1,E2), constraint: true
		
	
		// Model Constraints
		//model.add PredicateConstraint.Symmetric, on: SimEntityType;
		//model.add PredicateConstraint.Symmetric, on: SimProperty;
		//model.add PredicateConstraint.Symmetric, on: SimPropertyValue;
		//model.add PredicateConstraint.Symmetric, on: SimEntity;
	
		model.add rule: (Entity(E1)) >> SimEntity(E1,E1), constraint: true;
		model.add rule: (Property(P1)) >> SimProperty(P1,P1), constraint: true;
		model.add rule: (PropertyValue(V1)) >> SimPropertyValue(V1,V1), constraint: true;
		model.add rule: (EntityType(T1)) >> SimEntityType(T1,T1), constraint: true;
		
		model.add rule: (SimEntity(E1,E2)) >> SimEntity(E2,E1), constraint: true;
		model.add rule: (SimProperty(P1,P2)) >> SimProperty(P2,P1), constraint: true;
		model.add rule: (SimPropertyValue(V1,V2)) >> SimPropertyValue(V2,V1), constraint: true;
		model.add rule: (SimEntityType(T1,T2)) >> SimEntityType(T2,T1), constraint: true;
		
		//model.add rule: (SimEntity(E1,E2) & SimEntity(E2,E3)) >> SimEntity(E1,E3), constraint: true;
		//model.add rule: (SimProperty(P1,P2) & SimProperty(P2,P3)) >> SimProperty(P1,P3), constraint: true;
		//model.add rule: (SimPropertyValue(V1,V2) & SimPropertyValue(V2,V3)) >> SimPropertyValue(V1,V3), constraint: true;
		//model.add rule: (SimEntityType(T1,T2) & SimEntityType(T2,T3)) >> SimEntityType(T1,T3), constraint: true;
	
		
		model.add rule : ~SimEntity(E1,E2), weight: p["p13"];
		model.add rule : ~SimEntityType(ET1,ET2), weight: p["p14"];
		model.add rule : ~SimProperty(P1,P2), weight: p["p15"];
		model.add rule : ~SimPropertyValue(O1,O2), weight: p["p16"];
		
		
		return model;
}

def execusteWLearnLevel2(DataStore dataStore, PSLModel model,
		ConfigBundle configBundle, String dataroot, String gtPath) {

		def closedWorldPredicateSet = [
			Triple1, Triple2,
			ShortStringLit, DateLit, NumericLit, LongStringLit,
			HasShortLitPropertyValue, HasLongLitPropertyValue,
			HasDateLitPropertyValue, HasNumericLitPropertyValue,
			SimURI,
			SimLit, LnkPropertyValue,
			//LexPropertyValueSim,
			RDFSubject,
			RDFPredicate,
			RDFType,
			RDFSubjPredicate,
			RDFIsInstanceOf, Name, Label,
			Property,
			(Predicate)model.getPredicate("PropertyValue"),
			HasPropertyValue,
			HasDomain,
			EntityType,
			HasType,
			HasProperty,
			Entity] as Set;

	// declare the partitions
	Partition observedPart = new Partition(100);
	Partition gtPart = new Partition(101);
	Partition constraintInferPart = new Partition(102);
	//Partition level1InferPart = new Partition(301);
	Partition level2InferPart = new Partition(302);

	def observedMap =  [
		 ((Predicate)model.getPredicate("Entity")):[gtPath+"/GT_Entity.csv"],
		 ((Predicate)model.getPredicate("HasProperty")):[gtPath+"/GT_HasProperty.csv"],
		 ((Predicate)model.getPredicate("Property")):[gtPath+"/GT_Property.csv"],
		 ((Predicate)model.getPredicate("EntityType")):[gtPath+"/GT_EntityType.csv"],
		 ((Predicate)model.getPredicate("PropertyValue")):[gtPath+"/GT_PropertyValue.csv"],
		 ((Predicate)model.getPredicate("HasPropertyValue")):[gtPath+"/GT_HasPropertyValue.csv"],
		 ((Predicate)model.getPredicate("HasShortLitPropertyValue")):[gtPath+"/GT_HasShortLitPropertyValue.csv"],
		 ((Predicate)model.getPredicate("HasLongLitPropertyValue")):[gtPath+"/GT_HasLongLitPropertyValue.csv"],
		 ((Predicate)model.getPredicate("HasDateLitPropertyValue")):[gtPath+"/GT_HasDateLitPropertyValue.csv"],
		 ((Predicate)model.getPredicate("HasNumericLitPropertyValue")):[gtPath+"/GT_HasNumericLitPropertyValue.csv"],
		 ((Predicate)model.getPredicate("HasDomain")):[gtPath+"/GT_HasDomain.csv"],
		 ((Predicate)model.getPredicate("HasType")):[gtPath+"/GT_HasType.csv"]];
			 
	// load additional observed data
	PSLUtil.loadFromCSVWithTruthValue(dataStore, observedMap, observedPart);
	 	 
	// a set of target query predicates
	def gtPredSet = [SimEntity, SimEntityType, SimProperty, SimPropertyValue] as Set;
	
	def gtPredMap =  [
		((Predicate)model.getPredicate("SimPropertyValue")):[gtPath+"/GT_SimPropertyValue.csv"],
		((Predicate)model.getPredicate("SimProperty")):[gtPath+"/GT_SimProperty.csv"],
		((Predicate)model.getPredicate("SimEntity")):[gtPath+"/GT_SimEntity.csv"],
		((Predicate)model.getPredicate("SimEntityType")):[gtPath+"/GT_SimEntityType.csv"]];
	
	PSLUtil.loadFromCSVWithTruthValue(dataStore, gtPredMap, gtPart);
	 

	
	System.out.println "[info] running level2 weight learning...";

	Date start = new Date();

	logStart(2, start, dataroot);

	Database trainDB = dataStore.getDatabase(level2InferPart, 
		closedWorldPredicateSet, observedPart,constraintInferPart);
	
		
	Database truthDB = dataStore.getDatabase(gtPart, gtPredSet);

	LazyMaxLikelihoodMPE weightLearning = new LazyMaxLikelihoodMPE(model, trainDB, truthDB, configBundle);
	weightLearning.learn();
	weightLearning.close();

	Date stop = new Date();

	TimeDuration td = TimeCategory.minus( stop, start );

	System.out.println("[info] level2 weight learning finished in " + td);

	logEnd(2, stop, td, dataroot);

	println model;
	
	logModel(dataroot, model, 2);


}

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
	def writer = new File(dataroot + "/Model.txt").newWriter("UTF-8", true);
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
