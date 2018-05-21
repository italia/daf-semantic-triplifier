package daf.testing.impala

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

// THIS IS A WORKING EXAMPLE, for DAF-IMPALA + ONTOP
object ExampleImpalaANPRComuni extends App {

  object R2RMLExamples {

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

  val logger = LoggerFactory.getLogger(this.getClass)

  val db_driver = "com.cloudera.impala.jdbc41.Driver"
  val db_name = "gove__amministrazione"

  Class.forName(db_driver)

  val dsn = "jdbc:impala://slave4.platform.daf.gov.it:21050;SSL=1;SSLKeyStore=./ssl_impala/master-impala.jks;SSLKeyStorePwd=Ahdai5th;AuthMech=3;CAIssuedCertNamesMismatch=1"
  val usr = "aserafini"
  val pwd = "openD4ti"

  val db_conn = DriverManager.getConnection(dsn, usr, pwd)
  db_conn.close()

  // -------------------------------------------------------------------------------

  val r2rmlFile = new File("src/test/resources/r2rml/poc_anpr_comuni.r2rml.ttl").getAbsoluteFile;

  val r2rmlModel = loadR2RMLString(R2RMLExamples.anpr_comuni)

  val owlOntology = createOWLOntology()

  // TODO: automation of re-creation of test db
  val preferences = new QuestPreferences();
  preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
  preferences.setCurrentValueOf(QuestPreferences.DBNAME, db_name);
  preferences.setCurrentValueOf(QuestPreferences.JDBC_DRIVER, db_driver);
  preferences.setCurrentValueOf(QuestPreferences.JDBC_URL, dsn);
  preferences.setCurrentValueOf(QuestPreferences.DBUSER, usr);
  preferences.setCurrentValueOf(QuestPreferences.DBPASSWORD, pwd);

  preferences.setCurrentValueOf("org.obda.owlreformulationplatform.queryingAnnotationsInOntology", "false");

  // needed for Impala SQL
  preferences.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, "false");

  println("starting...")

  val start_time = LocalDateTime.now()

  val repo = new SesameVirtualRepo("test_repo", owlOntology, r2rmlModel, preferences);

  repo.initialize();

  val conn: RepositoryConnection = repo.getConnection()
  val vf = conn.getValueFactory

  val statements = conn.getStatements(null, null, null, false).asList()
    .toStream
    .map(normalize_value)

  val output_file = new File("target/EXPORT/testing_rdf.nt").getAbsoluteFile
  if (!output_file.getParentFile.exists()) output_file.getParentFile.mkdirs()
  val fos = new FileOutputStream(output_file)
  Rio.write(statements, fos, RDFFormat.NTRIPLES)
  fos.close()

  val end_time = LocalDateTime.now()

  val processing_time = Duration.create(end_time.getNano - start_time.getNano, TimeUnit.NANOSECONDS).toCoarsest

  logger.info(s"the file ${output_file.getName} was created in ${processing_time}")

  // releasing connection
  conn.close()

  repo.shutDown()

  val dump = Files.readAllLines(output_file.toPath())
    .toStream
    .zipWithIndex.map(_.swap)
    .map(x => s"${x._1}\t${x._2}")
    .slice(Random.nextInt(100), Random.nextInt(500)).mkString("\n")
  println("\n\n\n\nRDF DUMP")
  println(dump)

  println("#### RDF mapping - STOP")

  System.exit(0)

  // ---------------------------------------

  def normalize_value(st: Statement): Statement = {
    val _value = st.getObject.stringValue()
    val fixed_value = _value.toLowerCase().split(" ").map(_.capitalize).mkString(" ")
    val _obj = vf.createLiteral(fixed_value)
    val fix_st = vf.createStatement(st.getSubject, st.getPredicate, _obj, st.getContext)
    fix_st
  }

  def createOWLOntology(): OWLOntology = {
    val owlManager = OWLManager.createOWLOntologyManager();
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