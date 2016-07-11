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



import com.wordpress.chapter10.infer.external.EqualPrefixImpl
import com.wordpress.chapter10.infer.external.ExactLexicalURISimilarity
import com.wordpress.chapter10.infer.external.IsDictWordImpl
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


// we have 4 levels of inference
int level = Integer.parseInt(args[0]);

// the data root
String dataroot = args[1];

// call source (terminal or web)
String callSource = args[2];

System.err.println(callSource)

String rdfInput = dataroot + File.separator + "rdf" + File.separator;
String feedbackInput = dataroot + File.separator + "feedback" + File.separator;
String ontInput = dataroot + File.separator + "ont" + File.separator;
String gtInput = dataroot + File.separator + "gt" + File.separator;
String output = dataroot + File.separator + "output" + File.separator;
//String dbfiles = dataroot + File.separator + "db" + File.separator;
String dbfiles = "/media/black/";
String weights = dataroot + File.separator + ".." +
		File.separator + "weights" + File.separator;

////////////////////////////////////////////////////////////////////////////////

// initialize PSL environment configuration
ConfigManager configManager = ConfigManager.getManager();

ConfigBundle configBundle = configManager.getBundle("ldpaygo2");
configBundle.addProperty("rdbmsdatastore.usestringids", true);

String defaultPath  = System.getProperty("user.dir");

String dbpath       = configBundle.getString("dbpath",
		dbfiles + "ldpaygo2");

// clearDB is false so that we don't delete previous data
DataStore dataStore = new RDBMSDataStore(
		new H2DatabaseDriver(Type.Disk, dbpath, false),configBundle);

////////////////////////////////////////////////////////////////////////////////

PSLModel model;

def priorMap = PSLUtil.loadWeightMap(weights + "p.txt", 16, "p");
def weightMap = PSLUtil.loadWeightMap(weights + "w.txt", 29, "w");
def feedbackMap = PSLUtil.loadWeightMap(weights + "f.txt", 18, "f");
def ontWeightMap = PSLUtil.loadWeightMap(weights +  "o.txt", 23, "o");

boolean squared = true;

switch(level) {

	case 0:
	// constraint inference
		model = level0(dataStore);

		println model;

		execusteInferLevel0(dataStore, model, configBundle, output);
		break;

	case 1:

		model = level1(dataStore, weightMap, priorMap, ontWeightMap, feedbackMap, callSource, squared);
		println model;

		execusteInferLevel1(dataStore, model, configBundle, output);

		break;

	case 2:
		model = level2(dataStore, weightMap, priorMap, ontWeightMap, feedbackMap, callSource, squared);
		println model;

		execusteInferLevel2(dataStore, model, configBundle, output);

		break;
	case 3:
		model = level3(dataStore, weightMap, priorMap, ontWeightMap, feedbackMap, callSource, squared);
		println model;

		execusteInferLevel3(dataStore, model, configBundle, output);

		break;

	case 4:
		model = level4(dataStore, weightMap, priorMap, ontWeightMap, feedbackMap, callSource, squared);
		println model;

		execusteInferLevel4(dataStore, model, configBundle, output);

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

	m.add predicate: "SimURISub", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	m.add predicate: "Name", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	m.add predicate: "Label", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	m.add predicate: "LnkPropertyValue", types: [ArgumentType.UniqueID];

	// Ontology constructs inferred by constraints
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


	// external function
	m.add function: "fnShortLitSim"   , implementation: new LexicalShortStringSimilarity();
	m.add function: "fnLongLitSim"    , implementation: new LexicalLongStringSimilarity();
	m.add function: "fnDateLitSim"    , implementation: new LexicalDateStringSimilarity();
	m.add function: "fnNumericLitSim" , implementation: new LexicalNumericStringSimilarity();
	m.add function: "fnIndvURISim"    , implementation: new LexicalIndividualURISimilarity();
	m.add function: "fnPropURISim"    , implementation: new LexicalVocabURISimilarity();
	m.add function: "fnTypeURISim"    , implementation: new LexicalVocabURISimilarity();
	m.add function: "fnYagoType"      , implementation: new YagoTypeImpl();
	m.add function: "fnMetaVocab" 	  , implementation: new MetaVocabImpl();
	m.add function: "IsLabel"         , implementation: new IsLabelImpl();
	m.add function: "IsName"          , implementation: new IsNameImpl();
	m.add function: "fnMetaPredicate" , implementation: new MetaPredicateImpl();
	m.add function: "fnLexURISim"     , implementation: new LexicalURISimilarity();
	m.add function: "IsDictWord"      , implementation: new IsDictWordImpl();


	////////////////////////// rule declaration ////////////////////////
	// System.out.println "[info] \t\tDECLARING RULES...";

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

	m.add rule: (RDFSubject(S)) >> SimURISub(S,S), constraint: true;


	///////////////////////////////////////////////////////////////////////////
	m.add rule: (OntTriple(S, rdfTypeID, rdfsClassID) & ~fnMetaVocab(S)) >> OntType(S), constraint: true;
	m.add rule: (OntTriple(S, rdfTypeID, owlClassID) & ~fnMetaVocab(S)) >> OntType(S), constraint: true;
	m.add rule: (OntTriple(S, rdfTypeID, rdfPropertyID) & ~fnMetaVocab(S)) >> OntProperty(S), constraint: true;
	m.add rule: (OntTriple(S, rdfTypeID, owlDatatypePropertyID) & ~fnMetaVocab(S)) >> OntProperty(S), constraint: true;
	m.add rule: (OntTriple(S, rdfTypeID, owlObjectPropertyID) & ~fnMetaVocab(S)) >> OntProperty(S), constraint: true;
	m.add rule: (OntTriple(S, rdfTypeID, owlAnnotationPropertyID) & ~fnMetaVocab(S)) >> OntAnnotProperty(S), constraint: true;
	m.add rule: (OntTriple(P, rdfsdomainID, T) & ~fnMetaVocab(P) & ~fnMetaVocab(T) ) >> OntHasProperty(T,P), constraint: true;
	m.add rule: (OntTriple(C1, owlequivalentClassID, C2) & ~fnMetaVocab(C2) & ~fnMetaVocab(C1) ) >> OntEqTypes(C1,C2), constraint: true;
	m.add rule: (OntTriple(P1, owlequivalentPropertyID, P2) & ~fnMetaVocab(P1) & ~fnMetaVocab(P2) ) >> OntEqProps(P1,P2), constraint: true;
	m.add rule: (OntTriple(C1, rdfssubClassOf, C2) & ~fnMetaVocab(C2) & ~fnMetaVocab(C1) ) >> OntSubTypes(C1,C2), constraint: true;
	m.add rule: (OntTriple(C1, owldisjointWithID, C2) & ~fnMetaVocab(C2) & ~fnMetaVocab(C1) ) >> OntDisjointTypes(C1,C2), constraint: true;

	// enrich the ontology
	m.add rule: (OntDisjointTypes(A,B)) >> OntDisjointTypes(B,A), constraint: true;
	m.add rule: (OntEqTypes(A,B)) >> OntEqTypes(B,A), constraint: true;
	m.add rule: (OntEqTypes(A1,A2) & OntEqTypes(B1,B2) & OntDisjointTypes(A1,B1)) >> OntDisjointTypes(A2,B2), constraint: true;
	m.add rule: (OntEqTypes(A,B) & OntDisjointTypes(A,C)) >> OntDisjointTypes(B,C), constraint: true;
	m.add rule: (OntSubTypes(A1,A2) & OntSubTypes(A2,A3)) >> OntSubTypes(A1,A3), constraint: true;
	m.add rule: (OntSubTypes(AS,A) & OntSubTypes(BS,B) & OntDisjointTypes(A,B)) >> OntDisjointTypes(AS,BS), constraint: true;


	// SimURI for ontology types
	m.add rule: (RDFType(T) & OntType(OT) & fnTypeURISim(T,OT)) >> OntSimURI(T,OT), constraint: true;
	m.add rule: (RDFPredicate(P) & OntProperty(OP) & fnPropURISim(P,OP)) >> OntSimURI(P,OP), constraint: true;

	// SimURI for ontology types
	m.add rule: (OntType(OT) & RDFType(T) & fnTypeURISim(OT,T)) >> OntSimURI(OT,T), constraint: true;
	m.add rule: (OntProperty(OP) & RDFPredicate(P) & fnPropURISim(OP,P)) >> OntSimURI(OP,P), constraint: true;

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
		//((Predicate)model.getPredicate("RDFPredicateObj")):dataroot+"/RDFPredicateObj.csv",
		((Predicate)model.getPredicate("RDFSubjPredicate")):dataroot+"/RDFSubjPredicate.csv",
		((Predicate)model.getPredicate("RDFIsInstanceOf")):dataroot+"/RDFIsInstanceOf.csv",
		((Predicate)model.getPredicate("LnkPropertyValue")):dataroot+"/LnkPropertyValue.csv",
		((Predicate)model.getPredicate("SimLit")):dataroot+"/SimLit.csv",
		((Predicate)model.getPredicate("SimURI")):dataroot+"/SimURI.csv",
		((Predicate)model.getPredicate("Label")):dataroot+"/Label.csv",
		((Predicate)model.getPredicate("Name")):dataroot+"/Name.csv",

		((Predicate)model.getPredicate("OntHasProperty")):dataroot+"/OntHasProperty.csv",
		((Predicate)model.getPredicate("OntType")):dataroot+"/OntType.csv",
		((Predicate)model.getPredicate("OntProperty")):dataroot+"/OntProperty.csv",
		((Predicate)model.getPredicate("OntAnnotProperty")):dataroot+"/OntAnnotProperty.csv",
		((Predicate)model.getPredicate("OntEqTypes")):dataroot+"/OntEqTypes.csv",
		((Predicate)model.getPredicate("OntSubTypes")):dataroot+"/OntSubTypes.csv",
		((Predicate)model.getPredicate("OntDisjointTypes")):dataroot+"/OntDisjointTypes.csv",
		((Predicate)model.getPredicate("OntSimURI")):dataroot+"/OntSimURI.csv"];

	printResults(dataStore, constraintInferPart, predicateFileMap);

	def predicateFileMap1 = [
		((Predicate)model.getPredicate("OntTriple")):dataroot+"/OntTriple.csv"];

	printResults(dataStore, observedPart, predicateFileMap1);


}

///////////////////////////////////////////////////////////////////////////////

def level1(DataStore data, def w, def p, def ow, def fp, String callSource, boolean sq) {
	println sq;

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

	
	 model.add predicate: "Rel", types: [
		 ArgumentType.UniqueID,
		 ArgumentType.UniqueID,
		 ArgumentType.UniqueID
	 ];
	 

	/** similarity predicates **/
	model.add predicate: "SimURI", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	/** similarity predicates **/
	model.add predicate: "SimURISub", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];


	// external function
	if (callSource.equals("terminal")) {
		model.add function: "fnShortLitSim"   , implementation: new LexicalShortStringSimilarity();
		model.add function: "fnLongLitSim"    , implementation: new LexicalLongStringSimilarity();
		model.add function: "fnDateLitSim"    , implementation: new LexicalDateStringSimilarity();
		model.add function: "fnNumericLitSim" , implementation: new LexicalNumericStringSimilarity();
		model.add function: "fnIndvURISim"    , implementation: new LexicalIndividualURISimilarity();
		model.add function: "fnPropURISim"    , implementation: new LexicalVocabURISimilarity();
		model.add function: "fnTypeURISim"    , implementation: new LexicalVocabURISimilarity();
		model.add function: "fnYagoType"      , implementation: new YagoTypeImpl();
		model.add function: "fnMetaVocab" 	  , implementation: new MetaVocabImpl();
		model.add function: "IsLabel"         , implementation: new IsLabelImpl();
		model.add function: "IsName"          , implementation: new IsNameImpl();
		model.add function: "fnMetaPredicate" , implementation: new MetaPredicateImpl();
		model.add function: "fnLexURISim"     , implementation: new LexicalURISimilarity();
		model.add function: "IsDictWord"      , implementation: new IsDictWordImpl();

	}

	/** Set similarity **/
	model.add setcomparison: "SimURISet" , using: SetComparison.Equality, on : SimURISub;


	////////////////////////// rule declaration ////////////////////////
	
	// Entity
	model.add rule: (RDFSubject(S) & RDFIsInstanceOf(S,T) & ~RDFType(S) & ~RDFPredicate(S)) >> Entity(S), weight: w["w1"], squared: sq;
//	model.add rule: (RDFSubject(S) & RDFType(S)) >> ~Entity(S), constraint: true;
//	model.add rule: (RDFSubject(S) & RDFPredicate(S)) >> ~Entity(S), constraint: true;

	// property
	model.add rule: (RDFSubjPredicate(S,P) & RDFIsInstanceOf(S,T) & ~fnMetaVocab(P)) >> Property(P), weight: w["w2"], squared: sq;
//	model.add rule: (RDFPredicate(P) & fnMetaVocab(P)) >> ~Property(P), constraint: true;

	// EntityType
	model.add rule: (RDFIsInstanceOf(S,T) & ~RDFType(S) & ~RDFPredicate(S) & ~IsDictWord(T) & ~fnMetaVocab(T) & ~fnYagoType(T)) >> ~EntityType(T), weight: w["w3"], squared: sq;
	model.add rule: (RDFIsInstanceOf(S,T) & ~RDFType(S) & ~RDFPredicate(S) & IsDictWord(T) & ~fnMetaVocab(T) & ~fnYagoType(T)) >> EntityType(T), weight: w["w4"], squared: sq;
	model.add rule: (RDFIsInstanceOf(S,T) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(T) & ~fnYagoType(T)) >> EntityType(T), weight: w["w5"], squared: sq;
//	model.add rule: (RDFType(T) & fnMetaVocab(T)) >> ~EntityType(T), constraint: true;
//	model.add rule: (RDFType(T) & fnYagoType(T)) >> ~EntityType(T), constraint: true;

	// PropertyValue
	model.add rule: (Triple1(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T)) >> PropertyValue(O), weight: w["w6"], squared: sq;
	model.add rule: (Triple2(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T)) >> PropertyValue(O), weight: w["w7"], squared: sq;

	// HasPropertyValue
	model.add rule: (Triple1(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T)) >> HasPropertyValue(S,O), weight: w["w8"], squared: sq;
	model.add rule: (Triple2(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T)) >> HasPropertyValue(S,O), weight: w["w9"], squared: sq;

	model.add rule: (Triple2(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T) & ShortStringLit(O)) >> HasShortLitPropertyValue(S,O), weight: w["w10"], squared: sq;
	model.add rule: (Triple2(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T) & LongStringLit(O))  >> HasLongLitPropertyValue(S,O), weight: w["w11"], squared: sq;
	model.add rule: (Triple2(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T) & DateLit(O)) >> HasDateLitPropertyValue(S,O), weight: w["w12"], squared: sq;
	model.add rule: (Triple2(S,P,O) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(P) & RDFIsInstanceOf(S,T) & NumericLit(O)) >> HasNumericLitPropertyValue(S,O), weight: w["w13"], squared: sq;

	// HasDomain
	model.add rule: (Triple1(S,P,O) & Entity(S) & Property(P)) >> HasDomain(O,P), weight: w["w14"], squared: sq;
	model.add rule: (Triple2(S,P,O) & Entity(S) & Property(P)) >> HasDomain(O,P), weight: w["w15"], squared: sq;

	// HasType
	//model.add rule: (RDFIsInstanceOf(S,T) & ~RDFType(S) & ~RDFPredicate(S) & ~fnMetaVocab(T) & ~fnYagoType(T)) >> HasType(S,T), weight: w["w14"];
	model.add rule: (EntityType(T) & Entity(S)  & RDFIsInstanceOf(S,T) & ~fnMetaVocab(T) & ~fnYagoType(T)) >> HasType(S,T), weight: w["w16"], squared: sq;
//	model.add rule: (RDFIsInstanceOf(S,T) & fnMetaVocab(T)) >> ~HasType(S,T), constraint: true;
//	model.add rule: (RDFIsInstanceOf(S,T) & fnYagoType(T)) >> ~HasType(S,T), constraint: true;

	// HasProperty set similarity surrogate
	//model.add rule: (RDFSubjPredicate(S,P) & RDFIsInstanceOf(S,T) & ~fnMetaVocab(T) & ~fnYagoType(T) & ~fnMetaVocab(P)) >> (HasProperty(T,P)), weight: w["w17"];
	
	// HasProperty
	
	model.add rule: (RDFPredicate(P) & RDFType(T) &
	~fnMetaVocab(T) & ~fnYagoType(T) & ~fnMetaVocab(P) &
	SimURISet ({T.RDFIsInstanceOf(inv)} , {P.RDFSubjPredicate(inv)})) >> (HasProperty(T,P)), weight: w["w17"], squared: sq;
	
	
	//def owlSameAs = data.getUniqueID("owl:sameAs");

	// relationships inference
	//model.add rule: (Triple1(E1,P,E2) & HasType(E1,T1) & HasType(E2,T2) & ~fnMetaVocab(T1) & ~fnMetaVocab(T2) & ~fnYagoType(T1) & ~fnYagoType(T2) & ~fnMetaVocab(P)) >> (Rel(T1,P,T2)), weight: 10;
	//model.add rule: (Triple1(E,owlSameAs,E1) & Triple1(E1,P,E2) & HasType(E1,T1) & HasType(E2,T2) & ~fnMetaVocab(T1) & ~fnMetaVocab(T2) & ~fnYagoType(T1) & ~fnYagoType(T2) & ~fnMetaVocab(P)) >> (Rel(T1,P,T2)), weight: 10;
	//model.add rule: (Triple1(E1,owlSameAs,E) & Triple1(E1,P,E2) & HasType(E1,T1) & HasType(E2,T2) & ~fnMetaVocab(T1) & ~fnMetaVocab(T2) & ~fnYagoType(T1) & ~fnYagoType(T2) & ~fnMetaVocab(P)) >> (Rel(T1,P,T2)), weight: 10;
	
	//model.add rule: (Triple1(E1,owlSameAs,E2) & Triple1(E2,P,E3) & HasType(E1,T1) & HasType(E3,T3) & ~fnMetaVocab(T1) & ~fnMetaVocab(T3) & ~fnYagoType(T1) & ~fnYagoType(T3) & ~fnMetaVocab(P)) >> (Rel(T1,P,T3)), weight: 10;
	//model.add rule: (Triple1(E1,P,E2) & Triple1(E2,owlSameAs,E3) & HasType(E1,T1) & HasType(E3,T3) & ~fnMetaVocab(T1) & ~fnMetaVocab(T3) & ~fnYagoType(T1) & ~fnYagoType(T3) & ~fnMetaVocab(P)) >> (Rel(T1,P,T3)), weight: 10;
	//model.add rule: (Triple1(E1,P,E2) & Triple1(E3,owlSameAs,E2) & HasType(E1,T1) & HasType(E3,T3) & ~fnMetaVocab(T1) & ~fnMetaVocab(T3) & ~fnYagoType(T1) & ~fnYagoType(T3) & ~fnMetaVocab(P)) >> (Rel(T1,P,T3)), weight: 10;
	//model.add rule: (Triple1(E1,P,E2) & Triple1(E2,owlSameAs,E3) & HasType(E1,T1) & HasType(E2,T2) & ~fnMetaVocab(T1) & ~fnMetaVocab(T2) & ~fnYagoType(T1) & ~fnYagoType(T2) & ~fnMetaVocab(P)) >> (Rel(T1,P,T2)), weight: 10;
	

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
	

	// ontology rules
	// Entity
	model.add rule: (RDFIsInstanceOf(S,OT) & OntType(OT) & ~RDFType(S) & ~RDFPredicate(S)) >> Entity(S), weight: ow["o1"], squared: sq;
	model.add rule: (RDFIsInstanceOf(S,T) & OntType(OT) & ~RDFType(S) & ~RDFPredicate(S) & fnLexURISim(T,OT)) >> Entity(S), weight: ow["o2"], squared: sq;
	model.add rule: (RDFIsInstanceOf(S,T) & OntEqTypes(T,OT) & ~RDFType(S) & ~RDFPredicate(S)) >> Entity(S), weight: ow["o3"], squared: sq;
	
	// Property
	model.add rule: (Entity(S) & RDFSubjPredicate(S,OP) & OntProperty(OP) & ~fnMetaVocab(OP)) >> Property(OP), weight: ow["o4"], squared: sq;
	model.add rule: (Entity(S) & RDFSubjPredicate(S,P) & OntProperty(OP) & fnLexURISim(P,OP) & ~fnMetaVocab(P)) >> Property(P), weight: ow["o5"], squared: sq;
	model.add rule: (Entity(S) & RDFSubjPredicate(S,P) & OntEqProps(P,OP) & ~fnMetaVocab(P)) >> Property(P), weight: ow["o6"], squared: sq;
	
	// EntityType
	model.add rule: (RDFIsInstanceOf(S,OT) & OntType(OT) & ~fnMetaVocab(OT) & ~fnYagoType(OT)) >> EntityType(OT), weight: ow["o7"], squared: sq;
	model.add rule: (RDFIsInstanceOf(S,T) & OntType(OT) & fnLexURISim(T,OT) & ~fnMetaVocab(T) & ~fnYagoType(T)) >> EntityType(T), weight: ow["o8"], squared: sq;
	model.add rule: (RDFIsInstanceOf(S,T) & OntEqTypes(T,OT) & ~fnMetaVocab(T) & ~fnYagoType(T)) >> EntityType(T), weight: ow["o9"], squared: sq;
	
	// HasProperty
	model.add rule: (RDFSubjPredicate(S,OP) & RDFIsInstanceOf(S,OT) & OntHasProperty(OT,OP)) >> HasProperty(OT,OP), weight: ow["o10"], squared: sq;
	model.add rule: (RDFSubjPredicate(S,OP) & RDFIsInstanceOf(S,T) & OntHasProperty(OT,OP) & fnLexURISim(T,OT)) >> HasProperty(T,OP), weight: ow["o11"], squared: sq;
	model.add rule: (RDFSubjPredicate(S,P) & RDFIsInstanceOf(S,OT) & OntHasProperty(OT,OP) & fnLexURISim(P,OP)) >> HasProperty(OT,P), weight: ow["o12"], squared: sq;

	// HasType
	model.add rule: (RDFIsInstanceOf(S,OT) & OntType(OT) & ~RDFType(S) & ~RDFPredicate(S)) >> HasType(S,OT), weight: ow["o13"], squared: sq;
	model.add rule: (RDFIsInstanceOf(S,T) & OntType(OT) & ~RDFType(S) & ~RDFPredicate(S) & fnLexURISim(T,OT)) >> HasType(S,T), weight: ow["o14"], squared: sq;
	model.add rule: (RDFIsInstanceOf(S,T) & OntEqTypes(T,OT) & ~RDFType(S) & ~RDFPredicate(S)) >> HasType(S,T), weight: ow["o15"], squared: sq;

	// priors
	model.add rule : ~Entity(E), weight: p["p1"], squared: sq;
	model.add rule : ~EntityType(ET), weight: p["p2"], squared: sq;
	model.add rule : ~Property(P), weight: p["p3"], squared: sq;
//	model.add rule : ~PropertyValue(O), weight: p["p4"], squared: sq;
//	model.add rule : ~HasPropertyValue(S,O), weight: p["p5"], squared: sq;
//	model.add rule : ~HasDomain(O,P), weight: p["p6"], squared: sq;
	model.add rule : ~HasType(S,T), weight: p["p7"], squared: sq;
	model.add rule : ~HasProperty(S,T), weight: p["p8"], squared: sq;

//	model.add rule : ~HasShortLitPropertyValue(S,O), weight: p["p9"], squared: sq;
//	model.add rule : ~HasLongLitPropertyValue(S,O), weight: p["p10"], squared: sq;
//	model.add rule : ~HasDateLitPropertyValue(S,O), weight: p["p11"], squared: sq;
//	model.add rule : ~HasNumericLitPropertyValue(S,O), weight: p["p12"], squared: sq;


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
		OntDisjointTypes,
		EntityTypeFB,
		HasTypeFB,
		HasPropertyFB,
		SimPropertyFB,
		SimEntityTypeFB,
		SimEntityFB] as Set;

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
		((Predicate)model.getPredicate("HasProperty")):dataroot+"/HasProperty.csv",
		((Predicate)model.getPredicate("Rel")):dataroot+"/Rel.csv"];

	printResults(dataStore, level1InferPart, predicateFileMap);

}

///////////////////////////////////////////////////////////////////////////////

def level2(DataStore data, def w, def p, def ow, def fp, String callSource, boolean sq) {
	PSLModel model = new PSLModel(this, data);


	// observed data
	model.add predicate: "SimURI",  types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	// similarity Predicates
	model.add predicate: "SimPropertyValue", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];
	model.add predicate: "SimProperty", types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];


	/** set similarity **/
	model.add setcomparison: "SimURISet" , using: SetComparison.CrossEquality, on : SimURI;
	model.add setcomparison: "SimValueSet" , using: SetComparison.CrossEquality, on : SimPropertyValue;


	// external function
	if (callSource.equals("terminal")) {
		model.add function: "fnShortLitSim"   , implementation: new LexicalShortStringSimilarity();
		model.add function: "fnLongLitSim"    , implementation: new LexicalLongStringSimilarity();
		model.add function: "fnDateLitSim"    , implementation: new LexicalDateStringSimilarity();
		model.add function: "fnNumericLitSim" , implementation: new LexicalNumericStringSimilarity();
		model.add function: "fnIndvURISim"    , implementation: new LexicalIndividualURISimilarity();
		model.add function: "fnPropURISim"    , implementation: new LexicalVocabURISimilarity();
		model.add function: "fnTypeURISim"    , implementation: new LexicalVocabURISimilarity();
		model.add function: "fnYagoType"      , implementation: new YagoTypeImpl();
		model.add function: "fnMetaVocab" 	  , implementation: new MetaVocabImpl();
		model.add function: "IsLabel"         , implementation: new IsLabelImpl();
		model.add function: "IsName"          , implementation: new IsNameImpl();
		model.add function: "fnMetaPredicate" , implementation: new MetaPredicateImpl();
		model.add function: "fnLexURISim"     , implementation: new LexicalURISimilarity();
	}

	// SimPropertyValue
		model.add rule: (PropertyValue(V1) & PropertyValue(V2) & SimURI(V1,V2) & (V1-V2)) >> SimPropertyValue(V1,V2), weight: w["w18"], squared: sq;
		model.add rule: (PropertyValue(V1) & PropertyValue(V2) & SimLit(V1,V2) & (V1-V2)) >> SimPropertyValue(V1,V2), weight: w["w19"], squared: sq;
	
		// SimProperty
		// lexical similarity of properties
		model.add rule: (Property(P1) & Property(P2)  & SimURI(P1,P2) & (P1-P2)) >> SimProperty(P1,P2), weight: w["w20"], squared: sq;
	
		model.add rule: (Property(P1) & Property(P2) &
		SimValueSet({P1.HasDomain(inv)},{P2.HasDomain(inv)}) & (P1-P2)) >> SimProperty(P1,P2), weight: w["w21"], squared: sq;
	
	//	model.add rule: (Property(P1) & Property(P2) & HasDomain(V1,P1) & HasDomain(V2,P2) & SimPropertyValue(V1,V2)) >> SimProperty(P1,P2), weight: w["w21"], squared: sq;
	
		// Feedback rules
		model.add rule: (SimPropertyFB(P1,P2,UID,"yes")) >> SimProperty(P1,P2), weight: fp["f15"];
		model.add rule: (SimPropertyFB(P1,P2,UID,"no")) >> ~SimProperty(P1,P2), weight: fp["f16"];
		
		
		// ontology rules
		model.add rule: (Property(P1) & Property(P2) & OntEqProps(P1,P2)) >>  SimProperty(P1,P2), weight: ow["o16"], squared: sq;
		model.add rule: (Property(P1) & Property(P2) & OntEqProps(OP1,OP2) & OntSimURI(P1,OP1) & OntSimURI(P2,OP2)) >> SimProperty(P1,P2), weight: ow["o17"], squared: sq;
		model.add rule: (Property(P1) & Property(P2) & OntProperty(OP) & OntSimURI(P1,OP) & OntSimURI(P2,OP)) >> SimProperty(P1,P2), weight: ow["o18"], squared: sq;
		
	
		// Model Constraints
		model.add PredicateConstraint.Symmetric, on: SimProperty;
		model.add PredicateConstraint.Symmetric, on: SimPropertyValue;
	
		model.add rule: (Property(P1)) >> SimProperty(P1,P1), constraint: true;
		model.add rule: (PropertyValue(V1)) >> SimPropertyValue(V1,V1), constraint: true;
	
	
	
		model.add rule : ~SimProperty(P1,P2), weight: p["p13"], squared: sq;
	//	model.add rule : ~SimPropertyValue(O1,O2), weight: p["p14"], squared: sq;
	
	
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
		HasShortLitPropertyValue,
		HasLongLitPropertyValue,
		HasDateLitPropertyValue,
		HasNumericLitPropertyValue,
		SimURI,
		SimLit,
		LnkPropertyValue,
		//LexPropertyValueSim,
		RDFSubject,
		RDFPredicate,
		RDFType,
		RDFSubjPredicate,
		RDFIsInstanceOf,
		Property,
		(Predicate)model.getPredicate("PropertyValue"),
		HasPropertyValue,
		HasDomain,
		EntityType,
		HasType,
		HasProperty,
		Entity,
		OntSimURI,
		OntTriple,
		OntType,
		OntProperty,
		OntAnnotProperty,
		OntHasProperty,
		OntEqTypes,
		OntEqProps,
		OntSubTypes,
		OntDisjointTypes,
		EntityTypeFB,
		HasTypeFB,
		HasPropertyFB,
		SimPropertyFB,
		SimEntityTypeFB,
		SimEntityFB] as Set;

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
		((Predicate)model.getPredicate("SimProperty")):dataroot+"/SimProperty.csv",
		((Predicate)model.getPredicate("SimPropertyValue")):dataroot+"/SimPropertyValue.csv"];



	printResults(dataStore, level2InferPart, predicateFileMap);
}

///////////////////////////////////////////////////////////////////////////////

def level3(DataStore data, def w, def p, def ow, def fp, String callSource, boolean sq) {
	PSLModel model = new PSLModel(this, data);


	// observed data
	model.add predicate: "SimURI",  types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	// Similarity Predicates
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
	model.add setcomparison: "SimPropertySet" , using: SetComparison.CrossEquality, on : SimProperty;

	// external function
	if (callSource.equals("terminal")) {
		model.add function: "fnShortLitSim"   , implementation: new LexicalShortStringSimilarity();
		model.add function: "fnLongLitSim"    , implementation: new LexicalLongStringSimilarity();
		model.add function: "fnDateLitSim"    , implementation: new LexicalDateStringSimilarity();
		model.add function: "fnNumericLitSim" , implementation: new LexicalNumericStringSimilarity();
		model.add function: "fnIndvURISim"    , implementation: new LexicalIndividualURISimilarity();
		model.add function: "fnPropURISim"    , implementation: new LexicalVocabURISimilarity();
		model.add function: "fnTypeURISim"    , implementation: new LexicalVocabURISimilarity();
		model.add function: "fnYagoType"      , implementation: new YagoTypeImpl();
		model.add function: "fnMetaVocab" 	  , implementation: new MetaVocabImpl();
		model.add function: "IsLabel"         , implementation: new IsLabelImpl();
		model.add function: "IsName"          , implementation: new IsNameImpl();
		model.add function: "fnMetaPredicate" , implementation: new MetaPredicateImpl();
		model.add function: "fnLexURISim"     , implementation: new LexicalURISimilarity();
	}
	
	// SimEntityType
	model.add rule: (EntityType(T1) & EntityType(T2)  & SimURI(T1,T2) & (T1-T2) ) >> SimEntityType(T1,T2), weight: w["w22"], squared: sq;
	model.add rule: (EntityType(T1) & EntityType(T2) & SimPropertySet({T1.HasProperty},{T2.HasProperty}) & (T1-T2)) >> SimEntityType(T1,T2), weight: w["w23"], squared: sq;
//	model.add rule: (EntityType(T1) & EntityType(T2)  & HasProperty(T1,P1) & HasProperty(T2,P2) & SimProperty(P1,P2) & (T1-T2) & (T1^T2) ) >> SimEntityType(T1,T2), weight: w["w23"], squared: sq;

	// Feedback rules
	model.add rule: (SimEntityTypeFB(T1,T2,UID,"yes")) >> SimEntityType(T1,T2), weight: fp["f13"];
	model.add rule: (SimEntityTypeFB(T1,T2,UID,"no")) >> ~SimEntityType(T1,T2), weight: fp["f14"];
	
	// ontology rules
	model.add rule: (EntityType(T1) & EntityType(T2) & OntEqTypes(T1,T2)) >> SimEntityType(T1,T2), weight: ow["o19"], squared: sq;
	model.add rule: (EntityType(T1) & EntityType(T2) & OntType(OT) & OntSimURI(T1,OT) & OntSimURI(T2,OT)) >> SimEntityType(T1,T2), weight: ow["o20"], squared: sq;
	model.add rule: (EntityType(T1) & EntityType(T2) & OntEqTypes(OT1,OT2) & OntSimURI(T1,OT1) & OntSimURI(T2,OT2)) >> SimEntityType(T1,T2), weight: ow["o21"], squared: sq;
	
	// ontology constraints
	model.add rule: (EntityType(T1) & EntityType(T2) & OntDisjointTypes(T1,T2)) >> ~SimEntityType(T1,T2), constraint: true;
	model.add rule: (EntityType(T1) & EntityType(T2) & OntDisjointTypes(OT1,OT2) & OntSimURI(T1,OT1) & OntSimURI(T2,OT2)) >> ~SimEntityType(T1,T2), constraint: true;
	

	// Model Constraints
	model.add PredicateConstraint.Symmetric, on: SimEntityType;
	model.add PredicateConstraint.Symmetric, on: SimProperty;
	model.add PredicateConstraint.Symmetric, on: SimPropertyValue;


	model.add rule: (EntityType(T1)) >> SimEntityType(T1,T1), constraint: true;


	model.add rule : ~SimEntityType(ET1,ET2), weight: p["p15"], squared: sq;



	return model;
}

def execusteInferLevel3(DataStore dataStore, PSLModel model,
		ConfigBundle configBundle, String dataroot) {


	def closedWorldPredicateSet = [
		Triple1,
		Triple2,
		ShortStringLit,
		DateLit,
		NumericLit,
		LongStringLit,
		HasShortLitPropertyValue,
		HasLongLitPropertyValue,
		HasDateLitPropertyValue,
		HasNumericLitPropertyValue,
		SimURI,
		SimLit,
		LnkPropertyValue,
		//LexPropertyValueSim,
		RDFSubject,
		RDFPredicate,
		RDFType,
		RDFSubjPredicate,
		RDFIsInstanceOf,
		Property,
		(Predicate)model.getPredicate("PropertyValue"),
		HasPropertyValue,
		HasDomain,
		EntityType,
		HasType,
		HasProperty,
		Entity,
		SimProperty,
		SimPropertyValue,
		OntSimURI,
		OntTriple,
		OntType,
		OntProperty,
		OntAnnotProperty,
		OntHasProperty,
		OntEqTypes,
		OntEqProps,
		OntSubTypes,
		OntDisjointTypes,
		EntityTypeFB,
		HasTypeFB,
		HasPropertyFB,
		SimPropertyFB,
		SimEntityTypeFB,
		SimEntityFB] as Set;

	// declare the partitions
	Partition observedPart = new Partition(100);
	Partition constraintInferPart = new Partition(200);
	Partition level1InferPart = new Partition(301);
	Partition level2InferPart = new Partition(302);
	Partition level3InferPart = new Partition(303);

	System.out.println "[info] running level3 inference...";

	Date start = new Date();

	logStart(3, start, dataroot);

	Database inferredDB = dataStore.getDatabase(level3InferPart,
			closedWorldPredicateSet,
			observedPart,constraintInferPart, level1InferPart, level2InferPart);

	LazyMPEInference inference = new LazyMPEInference(model, inferredDB, configBundle);
	inference.mpeInference();
	inference.close();
	inferredDB.close();

	Date stop = new Date();

	TimeDuration td = TimeCategory.minus( stop, start );

	System.out.println("[info] level3 inference finished in " + td);

	logEnd(3, stop, td, dataroot);
	////////////////////////////////////////////////////////////////////////////////

	// Print the results
	def predicateFileMap = [
		((Predicate)model.getPredicate("SimEntityType")):dataroot+"/SimEntityType.csv"];




	printResults(dataStore, level3InferPart, predicateFileMap);
}

///////////////////////////////////////////////////////////////////////////////

def level4(DataStore data, def w, def p, def ow, def fp, String callSource, boolean sq) {
	PSLModel model = new PSLModel(this, data);


	// observed data
	model.add predicate: "SimURI",  types: [
		ArgumentType.UniqueID,
		ArgumentType.UniqueID
	];

	// Similarity Predicates
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
	model.add setcomparison: "SimPropertySet" , using: SetComparison.CrossEquality, on : SimProperty;
	model.add setcomparison: "SimTypeSet" , using: SetComparison.CrossEquality, on : SimEntityType;


	// external function
	if (callSource.equals("terminal")) {
		model.add function: "fnShortLitSim"   , implementation: new LexicalShortStringSimilarity();
		model.add function: "fnLongLitSim"    , implementation: new LexicalLongStringSimilarity();
		model.add function: "fnDateLitSim"    , implementation: new LexicalDateStringSimilarity();
		model.add function: "fnNumericLitSim" , implementation: new LexicalNumericStringSimilarity();
		model.add function: "fnIndvURISim"    , implementation: new LexicalIndividualURISimilarity();
		model.add function: "fnPropURISim"    , implementation: new LexicalVocabURISimilarity();
		model.add function: "fnTypeURISim"    , implementation: new LexicalVocabURISimilarity();
		model.add function: "fnYagoType"      , implementation: new YagoTypeImpl();
		model.add function: "fnMetaVocab" 	  , implementation: new MetaVocabImpl();
		model.add function: "IsLabel"         , implementation: new IsLabelImpl();
		model.add function: "IsName"          , implementation: new IsNameImpl();
		model.add function: "fnMetaPredicate" , implementation: new MetaPredicateImpl();
		model.add function: "fnLexURISim"     , implementation: new LexicalURISimilarity();
	}

	model.add function: "SimilarLocalNames" , implementation: new SimilarLocalNamesImpl();
	model.add function: "SimilarLabels", implementation: new SimilarLabelsImpl();
	model.add function: "SimilarNames", implementation: new SimilarNamesImpl();
	model.add function: "SimilarLiterals", implementation: new SimilarLiteralsImpl();
	
	model.add rule: (Entity(E1) & Entity(E2) & Label(E1, L1) & Label(E2, L2) & SimilarLabels(L1,L2) & (E1-E2) & (E1^E2)) >> SimEntity(E1,E2), weight: w["w24"], squared: sq;
	
		model.add rule: (Entity(E1) & Entity(E2) & Label(E1, L1) & Label(E2, L2) & ~SimilarLabels(L1,L2) & (E1-E2) & (E1^E2)) >> ~SimEntity(E1,E2), weight: w["w25"], squared: sq;
	
		model.add rule: (Entity(E1) & Entity(E2) & Name(E1, N1) & Name(E2, N2) & SimilarNames(N1,N2) & (E1-E2) & (E1^E2)) >> SimEntity(E1,E2), weight: w["w26"], squared: sq;
	
		model.add rule: (Entity(E1) & Entity(E2) & Name(E1, N1) & Name(E2, N2) & ~SimilarNames(N1,N2) & (E1-E2) & (E1^E2)) >> ~SimEntity(E1,E2), weight: w["w27"], squared: sq;
	
		model.add rule: (Entity(E1) & Entity(E2) & Triple1(E1, P1, O1) & Triple1(E2, P2, O2) & Property(P1) & Property(P2) & SimProperty(P1,P2) & fnLexURISim(O1,O2) & (E1-E2) & (E1^E2)) >> SimEntity(E1,E2), weight: w["w28"], squared: sq;
	
		model.add rule: (Entity(E1) & Entity(E2) & Triple2(E1, P1, O1) & Triple2(E2, P2, O2) & Property(P1) & Property(P2) & SimProperty(P1,P2) & SimilarLiterals(O1,O2) & (E1-E2) & (E1^E2)) >> SimEntity(E1,E2), weight: w["w29"], squared: sq;
	
	
		// Feedback rules
		model.add rule: (SimEntityFB(E1,E2,UID,"yes")) >> SimEntity(E1,E2), weight: fp["f17"];
		model.add rule: (SimEntityFB(E1,E2,UID,"no")) >> ~SimEntity(E1,E2), weight: fp["f18"];
	
	
		// ontology rules
	//	model.add rule: (Entity(E1) & Entity(E2) & HasType(E1,T1) & HasType(E2,T2) & OntEqTypes(T1,T2) & SimValueSet ({E1.HasPropertyValue} , {E2.HasPropertyValue})) >> SimEntity(E1,E2), weight: ow["o22"];
		
		
		//  ontology constraints
	//	model.add rule: (Entity(E1) & Entity(E2) & HasType(E1,T1) & HasType(E2,T2) & OntDisjointTypes(T1,T2)) >> ~SimEntity(E1,E2), constraint: true;
	//	model.add rule: (Entity(E1) & Entity(E2) & HasType(E1,T1) & HasType(E2,T2) & OntDisjointTypes(OT1,OT2) & OntSimURI(T1,OT1) & OntSimURI(T2,OT2)) >> ~SimEntity(E1,E2), constraint: true;
	
		// Model Constraints
		model.add PredicateConstraint.Symmetric, on: SimEntityType;
		model.add PredicateConstraint.Symmetric, on: SimProperty;
		model.add PredicateConstraint.Symmetric, on: SimPropertyValue;
		model.add PredicateConstraint.Symmetric, on: SimEntity;
	
		model.add rule: (Entity(E1)) >> SimEntity(E1,E1), constraint: true;
	
	
	
		model.add rule : ~SimEntity(E1,E2), weight: p["p16"], squared: sq;
	
	
	
		return model;
}

def execusteInferLevel4(DataStore dataStore, PSLModel model,
		ConfigBundle configBundle, String dataroot) {


	def closedWorldPredicateSet = [
		Triple1,
		Triple2,
		ShortStringLit,
		DateLit,
		NumericLit,
		LongStringLit,
		HasShortLitPropertyValue,
		HasLongLitPropertyValue,
		HasDateLitPropertyValue,
		HasNumericLitPropertyValue,
		SimURI,
		SimLit,
		LnkPropertyValue,
		//LexPropertyValueSim,
		RDFSubject,
		RDFPredicate,
		RDFType,
		RDFSubjPredicate,
		RDFIsInstanceOf,
		Name,
		Label,
		Property,
		(Predicate)model.getPredicate("PropertyValue"),
		HasPropertyValue,
		HasDomain,
		EntityType,
		HasType,
		HasProperty,
		Entity,
		SimProperty,
		SimPropertyValue,
		SimEntityType,
		OntSimURI,
		OntTriple,
		OntType,
		OntProperty,
		OntAnnotProperty,
		OntHasProperty,
		OntEqTypes,
		OntEqProps,
		OntSubTypes,
		OntDisjointTypes,
		EntityTypeFB,
		HasTypeFB,
		HasPropertyFB,
		SimPropertyFB,
		SimEntityTypeFB,
		SimEntityFB] as Set;

	// declare the partitions
	Partition observedPart = new Partition(100);
	Partition constraintInferPart = new Partition(200);
	Partition level1InferPart = new Partition(301);
	Partition level2InferPart = new Partition(302);
	Partition level3InferPart = new Partition(303);
	Partition level4InferPart = new Partition(304);

	System.out.println "[info] running level4 inference...";

	Date start = new Date();

	logStart(4, start, dataroot);

	Database inferredDB = dataStore.getDatabase(level4InferPart,
			closedWorldPredicateSet,
			observedPart,constraintInferPart, level1InferPart,
			level2InferPart, level3InferPart);

	LazyMPEInference inference = new LazyMPEInference(model, inferredDB, configBundle);
	inference.mpeInference();
	inference.close();
	inferredDB.close();

	Date stop = new Date();

	TimeDuration td = TimeCategory.minus( stop, start );

	System.out.println("[info] level4 inference finished in " + td);

	logEnd(4, stop, td, dataroot);
	////////////////////////////////////////////////////////////////////////////////

	// Print the results

	def predicateFileMap = [
		((Predicate)model.getPredicate("SimEntity")):dataroot+"/SimEntity.csv"];
		//((Predicate)model.getPredicate("Mystery")):dataroot+"/Mystery.csv"];



	printResults(dataStore, level4InferPart, predicateFileMap);
}

///////////////////////////////////////////////////////////////////////////////


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



