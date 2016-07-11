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

package com.wordpress.chapter10.infer



import com.wordpress.chapter10.infer.external.*;
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

//def weightMap = PSLUtil.loadWeightMap("data/weights/2level_infer_w.txt", 25, "w");
//def priorMap = PSLUtil.loadWeightMap("data/weights/2level_infer_p.txt", 16, "p");
//def ontWeightMap = PSLUtil.loadWeightMap("data/weights/2level_ont_infer_o_db.txt", 37, "o");

def weightMap = PSLUtil.loadWeightMap("data/weights/w.txt", 25, "w");
def priorMap = PSLUtil.loadWeightMap("data/weights/p.txt", 16, "p");
def ontWeightMap = PSLUtil.loadWeightMap("data/weights/o.txt", 37, "o");

switch(level) {

	case 0:
	// constraint inference
		model = level0(dataStore);
		println model;

		execusteInferLevel0(dataStore, model, configBundle, dataroot);
		break;

	case 1:
		model = level1(dataStore, weightMap, ontWeightMap, priorMap);

		execusteInferLevel1(dataStore, model, configBundle, dataroot);

		break;

	case 2:
		model = level2(dataStore, weightMap, ontWeightMap, priorMap);

		execusteInferLevel2(dataStore, model, configBundle, dataroot);

		break;

	default:
		println "Invalid Inference level";
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


///////////////////////// level 0 model & inference /////////////////////////

/**
 * Constraints Model
 */
def level0(DataStore data) {


	PSLModel m = new PSLModel(this, data);

	/** RDF constructs Inferred by constraints **/
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
		ArgumentType.UniqueID
	];

	m.add predicate: "RDFPredicateObj",  types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	m.add predicate: "RDFIsInstanceOf",  types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	m.add predicate: "SimLit", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];


	m.add predicate: "SimURI", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	m.add predicate: "LnkPropertyValue", types: [ArgumentType.UniqueID];

	/** Ontology constructs inferred by constraints **/
	m.add predicate: "OntType", types: [ArgumentType.UniqueID];
	m.add predicate: "OntProperty", types: [ArgumentType.UniqueID];
	m.add predicate: "OntAnnotProperty", types: [ArgumentType.UniqueID];
	m.add predicate: "OntHasProperty", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];
	m.add predicate: "OntEqTypes", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];
	m.add predicate: "OntSubTypes", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];
	m.add predicate: "OntDisjointTypes", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];
	m.add predicate: "OntSimURI", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];
	m.add predicate: "OntEqProps", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	/** external functions **/
	m.add function: "fnMetaVocab" , implementation: new MetaVocabImpl();
	m.add function: "fnLexSimURI" , implementation: new LexicalURISimilarity();
	m.add function: "fnShortLitSim"   , implementation: new LexicalShortStringSimilarity();
	m.add function: "fnLongLitSim"    , implementation: new LexicalLongStringSimilarity();
	m.add function: "fnDateLitSim"    , implementation: new LexicalDateStringSimilarity();
	m.add function: "fnNumericLitSim" , implementation: new LexicalNumericStringSimilarity();
	m.add function: "fnIndvURISim"    , implementation: new LexicalIndividualURISimilarity();
	m.add function: "fnPropURISim"    , implementation: new LexicalVocabURISimilarity();
	m.add function: "fnTypeURISim"    , implementation: new LexicalVocabURISimilarity();
	m.add function: "fnYagoType"      , implementation: new YagoTypeImpl();


	////////////////////////// rule declaration ////////////////////////

	// rdf:type construct and freebase alternative
	def rdfTypeID = data.getUniqueID("rdf:type");
	def freebaseTypeID = data.getUniqueID("freebase:type.object.type");

	// owl and rdf constructs used in rules
	def rdfsClassID = data.getUniqueID("rdfs:Class");
	def owlClassID = data.getUniqueID("owl:Class");
	def rdfPropertyID = data.getUniqueID("rdf:Property");
	def owlDatatypePropertyID = data.getUniqueID("owl:DatatypeProperty");
	def owlObjectPropertyID = data.getUniqueID("owl:ObjectProperty");
	def owlAnnotationPropertyID = data.getUniqueID("owl:AnnotationProperty");
	def rdfsdomainID = data.getUniqueID("rdfs:domain");
	def owlequivalentClassID = data.getUniqueID("owl:equivalentClass");
	def owldisjointWithID = data.getUniqueID("owl:disjointWith");
	def rdfssubClassOf = data.getUniqueID("rdfs:subClassOf");
	def owlequivalentPropertyID = data.getUniqueID("owl:equivalentProperty");

	m.add rule: (Triple1(S,P,O)) >> RDFSubject(S), constraint: true;

	m.add rule: (Triple2(S,P,O)) >> RDFSubject(S), constraint: true;

	m.add rule: (Triple1(S,P,O)) >> RDFPredicate(P), constraint: true;

	m.add rule: (Triple2(S,P,O)) >> RDFPredicate(P), constraint: true;

	m.add rule: (Triple1(S,rdfTypeID,owlClassID) ) >> RDFType(S), constraint: true;

	m.add rule: (Triple1(S,rdfTypeID,rdfsClassID) ) >> RDFType(S), constraint: true;

	m.add rule: (Triple1(S,rdfTypeID,O) ) >> RDFType(O), constraint: true;

	m.add rule: (Triple1(S,rdfTypeID,O) ) >> RDFIsInstanceOf(S,O), constraint: true;

	//m.add rule: (Triple1(S,freebaseTypeID,O) ) >> RDFType(O), constraint: true;

	//m.add rule: (Triple1(S,freebaseTypeID,O) ) >> RDFIsInstanceOf(S,O), constraint: true;

	m.add rule: (Triple1(S,P,O)) >> RDFSubjPredicate(S,P), constraint: true;

	m.add rule: (Triple2(S,P,O)) >> RDFSubjPredicate(S,P), constraint: true;

	m.add rule: (Triple1(S,P,O)) >> LnkPropertyValue(O), constraint: true;
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


	///////////////////////////////////////////////////////////////////////////
	m.add rule: (OntTriple(S, rdfTypeID, rdfsClassID)
	& ~fnMetaVocab(S)) >> OntType(S), constraint: true;


	m.add rule: (OntTriple(S, rdfTypeID, owlClassID)
	& ~fnMetaVocab(S)) >> OntType(S), constraint: true;


	m.add rule: (OntTriple(S, rdfTypeID, rdfPropertyID)
	& ~fnMetaVocab(S)) >> OntProperty(S), constraint: true;


	m.add rule: (OntTriple(S, rdfTypeID, owlDatatypePropertyID)
	& ~fnMetaVocab(S)) >> OntProperty(S), constraint: true;

	m.add rule: (OntTriple(S, rdfTypeID, owlObjectPropertyID)
	& ~fnMetaVocab(S)) >> OntProperty(S), constraint: true;


	m.add rule: (OntTriple(S, rdfTypeID, owlAnnotationPropertyID)
	& ~fnMetaVocab(S)) >> OntAnnotProperty(S), constraint: true;


	m.add rule: (OntTriple(P, rdfsdomainID, T)
	& ~fnMetaVocab(P) & ~fnMetaVocab(T) ) >> OntHasProperty(T,P), constraint: true;


	m.add rule: (OntTriple(C1, owlequivalentClassID, C2)
	& ~fnMetaVocab(C2) & ~fnMetaVocab(C1) ) >> OntEqTypes(C1,C2), constraint: true;

	m.add rule: (OntTriple(P1, owlequivalentPropertyID, P2)
	& ~fnMetaVocab(P1) & ~fnMetaVocab(P2) ) >> OntEqProps(P1,P2), constraint: true;


	m.add rule: (OntTriple(C1, rdfssubClassOf, C2)
	& ~fnMetaVocab(C2) & ~fnMetaVocab(C1) ) >> OntSubTypes(C1,C2), constraint: true;

	m.add rule: (OntTriple(C1, owldisjointWithID, C2)
	& ~fnMetaVocab(C2) & ~fnMetaVocab(C1) ) >> OntDisjointTypes(C1,C2), constraint: true;

	// enrich the ontology
	m.add rule: (OntEqTypes(A1,A2) & OntEqTypes(B1,B2)
	& OntDisjointTypes(A1,B1)) >> OntDisjointTypes(A2,B2), constraint: true;

	m.add rule: (OntSubTypes(A1,A2) & OntSubTypes(A2,A3)) >> OntSubTypes(A1,A3), constraint: true;

	m.add rule: (OntSubTypes(AS,A) & OntSubTypes(BS,B)
	& OntDisjointTypes(A,B)) >> OntDisjointTypes(AS,BS), constraint: true;


	// SimURI for ontology types
	m.add rule: (RDFType(T) & OntType(OT)
	& fnTypeURISim(T,OT)) >> OntSimURI(T,OT), constraint: true;

	m.add rule: (RDFPredicate(P) & OntProperty(OP)
	& fnPropURISim(P,OP)) >> OntSimURI(P,OP), constraint: true;

	// SimURI for ontology types
	m.add rule: (OntType(OT) & RDFType(T)
	& fnTypeURISim(OT,T)) >> OntSimURI(OT,T), constraint: true;

	m.add rule: (OntProperty(OP) & RDFPredicate(P)
	& fnPropURISim(OP,P)) >> OntSimURI(OP,P), constraint: true;


	m.add PredicateConstraint.Symmetric, on: SimLit;
	m.add PredicateConstraint.Symmetric, on: SimURI;
	m.add PredicateConstraint.Symmetric, on: OntSimURI;


	return m;

}


def execusteInferLevel0(DataStore dataStore, PSLModel model,
		ConfigBundle configBundle, String dataroot) {



	// observed predicates
	def closedWorldPredicateSet = [
		Triple1,
		Triple2,
		ShortStringLit,
		DateLit,
		NumericLit,
		LongStringLit] as Set;

	// declare the partitions
	Partition observedPart = new Partition(100);
	Partition constraintInferPart = new Partition(200);

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


	////////////////////////////////////////////////////////////////////////////////

	// Print the results

	def predicateFileMap = [
		((Predicate)model.getPredicate("RDFSubject")):dataroot+"/RDFSubject.csv",
		((Predicate)model.getPredicate("RDFPredicate")):dataroot+"/RDFPredicate.csv",
		((Predicate)model.getPredicate("RDFType")):dataroot+"/RDFType.csv",
		((Predicate)model.getPredicate("RDFSubjPredicate")):dataroot+"/RDFSubjPredicate.csv",
		((Predicate)model.getPredicate("RDFIsInstanceOf")):dataroot+"/RDFIsInstanceOf.csv",
		((Predicate)model.getPredicate("OntType")):dataroot+"/OntType.csv",
		((Predicate)model.getPredicate("OntProperty")):dataroot+"/OntProperty.csv",
		((Predicate)model.getPredicate("OntAnnotProperty")):dataroot+"/OntAnnotProperty.csv",
		((Predicate)model.getPredicate("OntEqTypes")):dataroot+"/OntEqTypes.csv",
		((Predicate)model.getPredicate("OntSubTypes")):dataroot+"/OntSubTypes.csv",
		((Predicate)model.getPredicate("OntDisjointTypes")):dataroot+"/OntDisjointTypes.csv",
		((Predicate)model.getPredicate("OntSimURI")):dataroot+"/OntSimURI.csv",
		((Predicate)model.getPredicate("SimURI")):dataroot+"/SimURI.csv",
		((Predicate)model.getPredicate("SimLit")):dataroot+"/SimLit.csv"];

	printResults(dataStore, constraintInferPart, predicateFileMap);

}

///////////////////////////////////////////////////////////////////////////////

def level1(DataStore data, def w, def ow, def p) {

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
	model.add function: "fnMetaType" , implementation: new MetaTypeImpl();
	model.add function: "fnMetaPredicate", implementation: new MetaPredicateImpl();
	model.add function: "fnMetaVocab" , implementation: new MetaVocabImpl();
	model.add function: "fnLexURISim" , implementation: new LexicalURISimilarity();
	model.add function: "fnEqPrefix" , implementation: new EqualPrefixImpl();
	model.add function: "fnPropURISim"    , implementation: new LexicalVocabURISimilarity();
	model.add function: "fnTypeURISim"    , implementation: new LexicalVocabURISimilarity();

	/** Set similarity **/
	model.add setcomparison: "SimURISet" , using: SetComparison.CrossEquality, on : SimURI;


	//////////////////////////////////////////////////////////////////////////////

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
	//model.add rule: (RDFPredicate(P) & fnMetaVocab(P)) >> ~Property(P), constraint: true;
	model.add rule: (Property(P)) >> ~fnMetaVocab(P), constraint: true;
	
	// EntityType
	model.add rule: (RDFIsInstanceOf(S,T) & ~RDFType(S) & ~RDFPredicate(S)) >> EntityType(T), weight: w["w3"];
	//model.add rule: (RDFType(T) & fnMetaVocab(T)) >> ~EntityType(T), constraint: true;
	//model.add rule: (RDFType(T) & fnYagoType(T)) >> ~EntityType(T), constraint: true;
	model.add rule: (EntityType(T)) >> ~fnYagoType(T), constraint: true;
	model.add rule: (EntityType(T)) >> ~fnMetaVocab(T), constraint: true;

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
	model.add rule: (RDFIsInstanceOf(S,T) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(T) & ~fnYagoType(T)) >> HasType(S,T), weight: w["w14"];
	//model.add rule: (EntityType(T) & Entity(S)  & RDFIsInstanceOf(S,T)) >> HasType(S,T), weight: w["w14"];
	model.add rule: (RDFIsInstanceOf(S,T) & fnMetaVocab(T)) >> ~HasType(S,T), constraint: true;
	model.add rule: (RDFIsInstanceOf(S,T) & fnYagoType(T)) >> ~HasType(S,T), constraint: true;

	// HasProperty
	model.add rule: (RDFPredicate(P) & RDFType(T) & /* fnEqPrefix(T,P) & */
	/*~fnMetaVocab(T) & ~fnYagoType(T) & ~fnMetaVocab(P) & */
	SimURISet ({T.RDFIsInstanceOf(inv)} , {P.RDFSubjPredicate(inv)})) >> (HasProperty(T,P)), weight: w["w15"];
	model.add rule: (HasProperty(T,P)) >> ~fnMetaVocab(P), constraint: true;
	model.add rule: (HasProperty(T,P)) >> ~fnMetaVocab(T), constraint: true;
	model.add rule: (HasProperty(T,P)) >> ~fnYagoType(T), constraint: true;



	// priors
	model.add rule : ~Entity(E), weight: p["p1"];
	model.add rule : ~EntityType(ET), weight: p["p2"];
	model.add rule : ~Property(P), weight: p["p3"];
	model.add rule : ~PropertyValue(O), weight: p["p4"];
	model.add rule : ~HasPropertyValue(S,O), weight: p["p5"];
	model.add rule : ~HasDomain(O,P), weight: p["p6"];
	model.add rule : ~HasType(S,T), weight: p["p7"];
	model.add rule : ~HasProperty(S,T), weight: p["p8"];
	/*
	 model.add rule : ~HasShortLitPropertyValue(S,O), weight: w["w24"];
	 model.add rule : ~HasLongLitPropertyValue(S,O), weight: w["w25"];
	 model.add rule : ~HasDateLitPropertyValue(S,O), weight: w["w26"];
	 model.add rule : ~HasNumericLitPropertyValue(S,O), weight: w["w27"];
	 */

	//////////////////////////////////////////////////////////////////////////////
	
	// Entity
	model.add rule: (RDFSubject(S) & RDFIsInstanceOf(S,OT)  & OntType(OT) ) >> Entity(S), weight: ow["o1"];
	model.add rule: (RDFSubject(S) & RDFIsInstanceOf(S,T) & RDFSubjPredicate(S,OP) & OntProperty(OP)) >> Entity(S), weight: ow["o2"];
	model.add rule: (RDFSubject(S) & RDFIsInstanceOf(S,T)  & OntType(OT) & fnLexURISim(T,OT)) >> Entity(S), weight: ow["o3"];
	model.add rule: (RDFSubject(S) &  RDFIsInstanceOf(S,T) & RDFSubjPredicate(S,P) & OntProperty(OP) & fnLexURISim(P,OP)) >> Entity(S), weight: ow["o4"];
	model.add rule: (RDFSubject(S) & RDFIsInstanceOf(S,T)  & OntEqTypes(T,OT)) >> Entity(S), weight: ow["o5"];
	model.add rule: (RDFSubject(S) & RDFIsInstanceOf(S,T) & RDFSubjPredicate(S,P) & OntEqProps(P,OP)) >> Entity(S), weight: ow["o6"];
	
	// Property
	model.add rule: (RDFSubjPredicate(S,OP) & OntProperty(OP)) >> Property(OP), weight: ow["o9"];
	model.add rule: (RDFSubjPredicate(S,P) & OntProperty(OP) & fnLexURISim(P,OP)) >> Property(P), weight: ow["o10"];
	model.add rule: (RDFSubjPredicate(S,P) & OntEqProps(P,OP)) >> Property(P), weight: ow["o11"];
	
	// EntityType
	model.add rule: (RDFIsInstanceOf(S,OT) & OntType(OT)) >> EntityType(OT), weight: ow["o13"];
	model.add rule: (RDFIsInstanceOf(S,T) & OntType(OT) & fnLexURISim(T,OT)) >> EntityType(T), weight: ow["o14"];
	model.add rule: (RDFIsInstanceOf(S,T) & OntEqTypes(T,OT) ) >> EntityType(T), weight: ow["o15"];
	
	// HasProperty
	model.add rule: (RDFSubjPredicate(S,OP) & RDFIsInstanceOf(S,OT) & OntHasProperty(OT,OP)) >> HasProperty(OT,OP), weight: ow["o17"];
	model.add rule: (RDFSubjPredicate(S,OP) & RDFIsInstanceOf(S,T) & OntHasProperty(OT,OP) & fnLexURISim(T,OT)) >> HasProperty(T,OP), weight: ow["o18"];
	model.add rule: (RDFSubjPredicate(S,P) & RDFIsInstanceOf(S,OT) & OntHasProperty(OT,OP) & fnLexURISim(P,OP)) >> HasProperty(OT,P), weight: ow["o19"];

	return model;

}


def execusteInferLevel1(DataStore dataStore, PSLModel model,
		ConfigBundle configBundle, String dataroot) {

	// observed predicates
	def closedWorldPredicateSet = [
		Triple1,
		Triple2,
		ShortStringLit,
		DateLit,
		NumericLit,
		LongStringLit,
		SimURI,
		SimLit,
		LnkPropertyValue,
		RDFSubject,
		RDFPredicate,
		RDFType,
		RDFSubjPredicate,
		RDFIsInstanceOf,
		OntSimURI,
		OntTriple,
		OntType,
		OntProperty,
		OntAnnotProperty,
		OntHasProperty,
		OntEqTypes,
		OntEqProps,
		OntSubTypes,
		OntDisjointTypes] as Set;

	// declare the partitions
	Partition observedPart = new Partition(100);
	Partition constraintInferPart = new Partition(200);
	Partition level1InferPart = new Partition(301);

	System.out.println "[info] running level1 inference...";

	Date start = new Date();

	logStart(1, start, dataroot);

	Database inferredDB = dataStore.getDatabase(level1InferPart,
			closedWorldPredicateSet,
			observedPart,constraintInferPart);

	LazyMPEInference inference = new LazyMPEInference(model, inferredDB, configBundle);
	inference.mpeInference();
	inference.close();
	inferredDB.close();

	Date stop = new Date();

	TimeDuration td = TimeCategory.minus( stop, start );

	System.out.println("[info] level1 inference finished in " + td);

	logEnd(1, stop, td, dataroot);

	////////////////////////////////////////////////////////////////////////////////

	// Print the results

	def predicateFileMap = [
		((Predicate)model.getPredicate("EntityType")):dataroot+"/EntityType.csv",
		((Predicate)model.getPredicate("Entity")):dataroot+"/Entity.csv",
		((Predicate)model.getPredicate("HasType")):dataroot+"/HasType.csv",
		((Predicate)model.getPredicate("Property")):dataroot+"/Property.csv",
		((Predicate)model.getPredicate("PropertyValue")):dataroot+"/PropertyValue.csv",
		((Predicate)model.getPredicate("HasDomain")):dataroot+"/HasDomain.csv",
		((Predicate)model.getPredicate("HasPropertyValue")):dataroot+"/HasPropertyValue.csv",
		((Predicate)model.getPredicate("HasShortLitPropertyValue")):dataroot+"/HasShortLitPropertyValue.csv",
		((Predicate)model.getPredicate("HasLongLitPropertyValue")):dataroot+"/HasLongLitPropertyValue.csv",
		((Predicate)model.getPredicate("HasDateLitPropertyValue")):dataroot+"/HasDateLitPropertyValue.csv",
		((Predicate)model.getPredicate("HasNumericLitPropertyValue")):dataroot+"/HasNumericLitPropertyValue.csv",
		((Predicate)model.getPredicate("HasProperty")):dataroot+"/HasProperty.csv"];

	printResults(dataStore, level1InferPart, predicateFileMap);

}

///////////////////////////////////////////////////////////////////////////////

def level2(DataStore data, def w, def ow, def p) {

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

	/** set similarity **/
	model.add setcomparison: "SimURISet" , using: SetComparison.CrossEquality, on : SimURI;
	model.add setcomparison: "SimValueSet" , using: SetComparison.CrossEquality, on : SimPropertyValue;
	model.add setcomparison: "SimPropertySet" , using: SetComparison.CrossEquality, on : SimProperty;
	model.add setcomparison: "SimTypeSet" , using: SetComparison.CrossEquality, on : SimEntityType;
	model.add setcomparison: "SimURISetOnt" , using: SetComparison.CrossEquality, on : OntSimURI;
	//model.add setcomparison: "SimValueSet" , using: SetComparison.CrossEquality, on : LexPropertyValueSim;

	/** External functions **/
	model.add function: "fnYagoType" , implementation: new YagoTypeImpl();
	model.add function: "fnMetaType" , implementation: new MetaTypeImpl();
	model.add function: "fnMetaPredicate", implementation: new MetaPredicateImpl();
	model.add function: "fnLexURISim" , implementation: new LexicalURISimilarity();

	// SimPropertyValue
	model.add rule: (PropertyValue(V1) & PropertyValue(V2) & SimURI(V1,V2) & (V1-V2)) >> SimPropertyValue(V1,V2), weight: w["w16"];
	model.add rule: (PropertyValue(V1) & PropertyValue(V2) & SimLit(V1,V2) & (V1-V2)) >> SimPropertyValue(V1,V2), weight: w["w17"];

	// SimProperty
	// lexical similarity of properties
	model.add rule: (Property(P1) & Property(P2)  & SimURI(P1,P2) & (P1-P2)) >> SimProperty(P1,P2), weight: w["w18"];

	model.add rule: (Property(P1) & Property(P2) &
	SimValueSet({P1.HasDomain(inv)},{P2.HasDomain(inv)}) & (P1-P2)) >> SimProperty(P1,P2), weight: w["w19"];

	// SimEntityType
	model.add rule: (EntityType(T1) & EntityType(T2)  & SimURI(T1,T2) & (T1-T2) ) >> SimEntityType(T1,T2), weight: w["w20"];

	model.add rule: (EntityType(T1) & EntityType(T2) & SimPropertySet({T1.HasProperty},{T2.HasProperty}) & (T1-T2)) >> SimEntityType(T1,T2), weight: w["w21"];


	// SimEntity
	model.add rule: (Entity(E1) & Entity(E2) &
	SimValueSet ({E1.HasShortLitPropertyValue} , {E2.HasShortLitPropertyValue}) & (E1-E2)) >> SimEntity(E1,E2), weight: w["w22"];

	//model.add rule: (HasType(E1,T1)  & HasType(E2,T2) & SimEntityType(T1,T2) & (E1-E2)) >> SimEntity(E1,E2), weight: w["w22"]-1;

	//model.add rule: (Entity(E1) & Entity(E2) &
	//SimValueSet ({E1.HasLongLitPropertyValue} , {E2.HasLongLitPropertyValue}) & (E1-E2)) >> SimEntity(E1,E2), weight: w["w23"];

	//model.add rule: (Entity(E1) & Entity(E2) &
	//SimValueSet ({E1.HasDateLitPropertyValue} , {E2.HasDateLitPropertyValue}) & (E1-E2)) >> SimEntity(E1,E2), weight: w["w24"];


	//model.add rule: (Entity(E1) & Entity(E2) &
	//SimValueSet ({E1.HasNumericLitPropertyValue} , {E2.HasNumericLitPropertyValue}) & (E1-E2)) >> SimEntity(E1,E2), weight: w["w25"];


	//model.add rule: (Entity(E1) & Entity(E2) & SimValueSet ({E1.HasPropertyValue} , {E2.HasPropertyValue})
	//	& SimTypeSet ({E1.HasType} , {E2.HasType})) >> SimEntity(E1,E2), weight: w["w26"];


	model.add PredicateConstraint.Symmetric, on: SimEntityType;
	model.add PredicateConstraint.Symmetric, on: SimProperty;
	model.add PredicateConstraint.Symmetric, on: SimPropertyValue;
	model.add PredicateConstraint.Symmetric, on: SimEntity;

	model.add rule: (Entity(E1)) >> SimEntity(E1,E1), constraint: true;
	model.add rule: (Property(P1)) >> SimProperty(P1,P1), constraint: true;
	model.add rule: (PropertyValue(V1)) >> SimPropertyValue(V1,V1), constraint: true;
	model.add rule: (EntityType(T1)) >> SimEntityType(T1,T1), constraint: true;


	model.add rule : ~SimEntity(E1,E2), weight: p["p13"];
	model.add rule : ~SimEntityType(ET1,ET2), weight: p["p14"];
	model.add rule : ~SimProperty(P1,P2), weight: p["p15"];
	//model.add rule : ~SimPropertyValue(O1,O2), weight: p["p16"];

	// ----------------- Ontology Rules ----------------------
	// SimProperty
	model.add rule: (Property(P1) & Property(P2) & OntEqProps(P1,P2)) >>  SimProperty(P1,P2), weight: ow["o23"];
	model.add rule: (Property(P1) & Property(P2) & OntEqProps(OP1,OP2) & OntSimURI(P1,OP1) & OntSimURI(P2,OP2)) >> SimProperty(P1,P2), weight: ow["o24"];
	model.add rule: (Property(P1) & Property(P2) & OntProperty(OP) & OntSimURI(P1,OP) & OntSimURI(P2,OP)) >> SimProperty(P1,P2), weight: ow["o25"];
	
	
	// SimEntityType
	model.add rule: (EntityType(T1) & EntityType(T2) & OntEqTypes(T1,T2)) >> SimEntityType(T1,T2), weight: ow["o28"];
	model.add rule: (EntityType(T1) & EntityType(T2) & OntType(OT) & OntSimURI(T1,OT) & OntSimURI(T2,OT)) >> SimEntityType(T1,T2), weight: ow["o29"];
	model.add rule: (EntityType(T1) & EntityType(T2) & OntEqTypes(OT1,OT2) & OntSimURI(T1,OT1) & OntSimURI(T2,OT2)) >> SimEntityType(T1,T2), weight: ow["o30"];
	model.add rule: (EntityType(T1) & EntityType(T2) & OntDisjointTypes(T1,T2)) >> ~SimEntityType(T1,T2), weight: ow["o31"];
	model.add rule: (EntityType(T1) & EntityType(T2) & OntDisjointTypes(OT1,OT2) & OntSimURI(T1,OT1) & OntSimURI(T2,OT2)) >> ~SimEntityType(T1,T2), weight: ow["o32"];
	
	// SimEntity
	model.add rule: (Entity(E1) & Entity(E2) & HasType(E1,T1) & HasType(E2,T2) & OntEqTypes(T1,T2) & SimValueSet ({E1.HasPropertyValue} , {E2.HasPropertyValue})) >> SimEntity(E1,E2), weight: ow["o33"];
	model.add rule: (Entity(E1) & Entity(E2) & HasType(E1,T1) & HasType(E2,T2) & OntDisjointTypes(T1,T2)) >> ~SimEntity(E1,E2), weight: ow["o36"];
	model.add rule: (Entity(E1) & Entity(E2) & HasType(E1,T1) & HasType(E2,T2) & OntDisjointTypes(OT1,OT2) & OntSimURI(T1,OT1) & OntSimURI(T2,OT2)) >> ~SimEntity(E1,E2), weight: ow["o37"];

	return model;
}

def execusteInferLevel2(DataStore dataStore, PSLModel model,
		ConfigBundle configBundle, String dataroot) {



	def closedWorldPredicateSet = [
		Triple1,
		Triple2,
		ShortStringLit,
		DateLit,
		NumericLit,
		LongStringLit,
		SimURI,
		SimLit,
		LnkPropertyValue,
		RDFSubject,
		RDFPredicate,
		RDFType,
		RDFSubjPredicate,
		RDFIsInstanceOf,
		OntSimURI,
		Property,
		(Predicate)model.getPredicate("PropertyValue"),
		HasPropertyValue,
		HasDomain,
		EntityType,
		HasType,
		HasProperty,
		Entity,
		OntTriple,
		OntType,
		OntProperty,
		OntAnnotProperty,
		OntHasProperty,
		OntEqTypes,
		OntEqProps,
		OntSubTypes,
		OntDisjointTypes] as Set;

	// declare the partitions
	Partition observedPart = new Partition(100);
	Partition constraintInferPart = new Partition(200);
	Partition level1InferPart = new Partition(301);
	Partition level2InferPart = new Partition(302);

	System.out.println "[info] running level2 inference...";

	Date start = new Date();

	logStart(2, start, dataroot);

	Database inferredDB = dataStore.getDatabase(level2InferPart,
			closedWorldPredicateSet,
			observedPart,constraintInferPart, level1InferPart);

	LazyMPEInference inference = new LazyMPEInference(model, inferredDB, configBundle);
	inference.mpeInference();
	inference.close();
	inferredDB.close();

	Date stop = new Date();

	TimeDuration td = TimeCategory.minus( stop, start );

	System.out.println("[info] level2 inference finished in " + td);

	logEnd(2, stop, td, dataroot);
	////////////////////////////////////////////////////////////////////////////////

	// Print the results

	def predicateFileMap = [
		((Predicate)model.getPredicate("SimEntity")):dataroot+"/SimEntity.csv",
		((Predicate)model.getPredicate("SimEntityType")):dataroot+"/SimEntityType.csv",
		((Predicate)model.getPredicate("SimProperty")):dataroot+"/SimProperty.csv",
		((Predicate)model.getPredicate("SimPropertyValue")):dataroot+"/SimPropertyValue.csv"];



	printResults(dataStore, level2InferPart, predicateFileMap);
}

///////////////////////////////////////////////////////////////////////////////


def logStart(int level, Date start, String path) {

	def writer = new File(path + "/infer_log.txt").newWriter("UTF-8", true);
	writer.write("" + start + "\t\tLevel" + level + " running inference\n");
	writer.close();

}

def logEnd(int level, Date stop, TimeDuration td, String path) {
	def writer = new File(path + "/infer_log.txt").newWriter("UTF-8", true);
	writer.write("" + stop + "\t\tLevel" + level + "  inference finished in: " + td + "\n");
	writer.close();
}



