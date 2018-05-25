package daf.examples

import it.unibz.inf.ontop.sesame.SesameVirtualRepo
import org.slf4j.LoggerFactory
import java.sql.DriverManager
import java.io.File
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences
import java.time.LocalDateTime
import org.openrdf.repository.RepositoryConnection
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants
import java.io.FileOutputStream
import org.openrdf.rio.RDFFormat
import org.openrdf.rio.Rio
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import java.nio.file.Files
import scala.util.Random
import org.openrdf.model.Statement
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.apibinding.OWLManager
import org.openrdf.model.impl.LinkedHashModel
import org.openrdf.model.Model
import org.openrdf.rio.RDFParser
import org.openrdf.rio.helpers.StatementCollector
import java.io.FileInputStream
import scala.io.Source
import java.io.ByteArrayInputStream
import java.io.OutputStream
import java.nio.file.Paths
import org.openrdf.model.ValueFactory
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import info.aduna.iteration.Iterations

// THIS IS A WORKING EXAMPLE, for DAF-IMPALA + ONTOP
object ExampleImpalaANPRComuni extends App {

  val conf = ConfigFactory.empty()

  val dump = "target/EXPORT/testing_rdf.nt"

  val ontop = new OntopWrapper(conf)

  println("#### RDF mapping - START")

  ontop.writeToFile(dump)

  val preview = ontop.previewDump(dump)

  println("\n\n\n\nRDF DUMP")
  println(preview)

  println("#### RDF mapping - STOP")
  System.exit(0)

}

class OntopWrapper(conf: Config) {

  val logger = LoggerFactory.getLogger(this.getClass)

  val db_driver = "com.cloudera.impala.jdbc41.Driver"
  val db_name = "gove__amministrazione"

  Class.forName(db_driver)

  val dsn = "jdbc:impala://slave4.platform.daf.gov.it:21050;SSL=1;SSLKeyStore=./ssl_impala/master-impala.jks;SSLKeyStorePwd=Ahdai5th;AuthMech=3;CAIssuedCertNamesMismatch=1"
  val usr = "aserafini"
  val pwd = "openD4ti"

  val db_conn = DriverManager.getConnection(dsn, usr, pwd)
  db_conn.close()

  //  val r2rmlModel = loadR2RMLString(R2RMLExamples.anpr_comuni)
  val r2rmlModel = loadR2RMLString(R2RMLExamples.test01)

  val owlOntology = createOWLOntology()

  // TODO: automation of re-creation of test db
  val preferences = new QuestPreferences()
  preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL)
  preferences.setCurrentValueOf(QuestPreferences.DBNAME, db_name)
  preferences.setCurrentValueOf(QuestPreferences.JDBC_DRIVER, db_driver)
  preferences.setCurrentValueOf(QuestPreferences.JDBC_URL, dsn)
  preferences.setCurrentValueOf(QuestPreferences.DBUSER, usr)
  preferences.setCurrentValueOf(QuestPreferences.DBPASSWORD, pwd)

  //  preferences.setCurrentValueOf("org.obda.owlreformulationplatform.queryingAnnotationsInOntology", "false")
  preferences.setCurrentValueOf(QuestPreferences.ANNOTATIONS_IN_ONTO, "false")
  preferences.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED)
  preferences.setCurrentValueOf(QuestPreferences.KEEP_ALIVE, "true")

  // needed for Impala SQL
  preferences.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, "false") // AVOID REPLACE !!

  var vf: ValueFactory = null

  def process(out: OutputStream) {

    val start_time = LocalDateTime.now()

    val repo = new SesameVirtualRepo("test_repo", owlOntology, r2rmlModel, preferences)

    repo.initialize()

    val conn: RepositoryConnection = repo.getConnection()
    vf = conn.getValueFactory

    val statements = Iterations.asList(conn.getStatements(null, null, null, true))

    Rio.write(statements, out, RDFFormat.NTRIPLES)

    val size = statements.size

    val end_time = LocalDateTime.now()

    val processing_time = Duration.create(end_time.getNano - start_time.getNano, TimeUnit.NANOSECONDS).toCoarsest

    logger.info(s"${size} triples created in ${processing_time}")

    conn.close()

    repo.shutDown()

  }

  def writeToFile(rdfFileName: String) {

    val output_file = new File(rdfFileName).getAbsoluteFile
    if (!output_file.getParentFile.exists()) output_file.getParentFile.mkdirs()
    val fos = new FileOutputStream(output_file)
    process(fos)
    fos.close()

  }

  def previewDump(rdfFileName: String): String = {

    Files.readAllLines(Paths.get(rdfFileName).toAbsolutePath().normalize())
      .toStream
      .zipWithIndex.map(_.swap)
      .map(x => s"${x._1}\t${x._2}")
      .slice(Random.nextInt(100), Random.nextInt(500)).mkString("\n")

  }

  def normalize_value(st: Statement): Statement = {

    val _value = st.getObject.stringValue()
    val fixed_value = _value.toLowerCase().split(" ").map(_.capitalize).mkString(" ")
    val _obj = vf.createLiteral(fixed_value)
    val fix_st = vf.createStatement(st.getSubject, st.getPredicate, _obj, st.getContext)
    fix_st
  }

  def createOWLOntology(): OWLOntology = {
    val owlManager = OWLManager.createOWLOntologyManager()
    owlManager.createOntology()
  }

  def loadOWLOntology(owlFile: String): OWLOntology = {
    val owlManager = OWLManager.createOWLOntologyManager();
    owlManager.loadOntologyFromOntologyDocument(new File(owlFile));
  }

  def loadR2RMLString(r2rml: String): Model = {

    val rdfParser: RDFParser = Rio.createParser(RDFFormat.TURTLE);
    val r2rmlModel: Model = new LinkedHashModel();
    val collector = new StatementCollector(r2rmlModel);
    rdfParser.setRDFHandler(collector);
    val bais = new ByteArrayInputStream(r2rml.getBytes)
    rdfParser.parse(bais, "http://example.org");
    bais.close()
    r2rmlModel
  }

  def loadR2RMLFile(r2rmlFile: String): Model = {
    val rdfParser: RDFParser = Rio.createParser(RDFFormat.TURTLE);
    val r2rmlModel: Model = new LinkedHashModel();
    val collector = new StatementCollector(r2rmlModel);
    rdfParser.setRDFHandler(collector);
    val fis = new FileInputStream(new File(r2rmlFile));
    rdfParser.parse(fis, "http://example.org");
    r2rmlModel
  }

  def loadSPARQL(sparqlFile: String): String = {
    val src = Source.fromFile(sparqlFile, "UTF-8")
    val txt = src.getLines().mkString("\n")
    src.close()
    txt
  }

}

object R2RMLExamples {

  def test01 = """
  
@prefix rr: <http://www.w3.org/ns/r2rml#> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .

@prefix l0: <http://w3id.org/italia/onto/l0/> .
@prefix clvapit: <http://w3id.org/italia/onto/CLV/> .

@base  <https://w3id.org/italia/> .

<Map_Regione> a rr:TriplesMapClass ;
		
	rr:logicalTable [ rr:tableName "gove__amministrazione.default_org_o_anpr_archivio_storico_comuni" ];
	
	rr:subjectMap [ 
		rr:template "http://w3id.org/italia/territorial-classifications/regions/{'idregione'}" ;
		rr:class skos:Concept, clvapit:Region, clvapit:Feature, clvapit:AddressComponent, clvapit:NamedAddressComponent ;
	] ;
	
	rr:predicateObjectMap [ 
	  rr:predicate l0:name ; 
	  rr:objectMap [ rr:column "idregione" ; rr:language "it" ]
	] ;

	rr:predicateObjectMap [ 
	  rr:predicate clvapit:situatedWithin ; 
	  rr:objectMap [ 
	  	rr:template "http://w3id.org/italia/territorial-classifications/countries/ITA" ;
	  	rr:termType rr:IRI ;
	  ] 
	] ;
	
	rr:predicateObjectMap [ 
	  rr:predicate clvapit:hasIdentifier ; 
	  rr:objectMap [
	  	rr:template "http://w3id.org/italia/identifiers/cr-{'idregione'}" ;
	  	rr:termType rr:IRI ;
	  ] 
	] ;
		
 .

 
<Map_CodiceRegione> a rr:TriplesMapClass ;
	
	rr:logicalTable [ rr:tableName "gove__amministrazione.default_org_o_anpr_archivio_storico_comuni" ];
	
	rr:subjectMap [ 
		rr:template "http://w3id.org/italia/identifiers/cr-{'idregione'}" ;
		rr:class clvapit:Identifier ;
	] ;
	
	rr:predicateObjectMap [ 
	  rr:predicate l0:identifier ; 
	  rr:objectMap [ rr:column "idregione" ]
	] ;

	rr:predicateObjectMap [ 
	  rr:predicate clvapit:identifierType ; 
	  rr:objectMap [ rr:constant "Codice Regione" ] 
	] ;
	
 .
    
  """

  def anpr_comuni = """
    
    @prefix rr: <http://www.w3.org/ns/r2rml#>.
    @prefix ex: <http://example.com/ns#>.
    
    <TriplesMap1> a rr:TriplesMapClass;

    rr:logicalTable [ rr:tableName "gove__amministrazione.default_org_o_anpr_archivio_storico_comuni" ];
    
    rr:subjectMap [
    	rr:template "http://w3id.org/italia/resources/comuni/{id}" ;
    	rr:class ex:City ;
    ] ;
    
    rr:predicateObjectMap [ 
      rr:predicate ex:name; 
      rr:objectMap [
      	rr:column "denominazione_it" ;
      ]
    ] ;

    .

  """

}