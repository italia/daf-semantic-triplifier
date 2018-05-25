package daf.examples
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences
import it.unibz.inf.ontop.sesame.RepositoryConnection
import it.unibz.inf.ontop.sesame.SesameVirtualRepo
import org.openrdf.model.Model
import org.openrdf.model.impl.LinkedHashModel
import org.openrdf.rio.RDFFormat
import org.openrdf.rio.RDFParser
import org.openrdf.rio.helpers.StatementCollector
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLOntology
import java.io.File
import java.io.FileInputStream
import scala.io.Source
import java.sql.DriverManager
import org.openrdf.model.Statement
import org.openrdf.rio.Rio
import java.io.FileOutputStream
import scala.concurrent.duration.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import java.nio.file.Files
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.util.Random
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

///**
// * CHECK:
// * Exception in thread "main" java.sql.SQLException: [Simba][ImpalaJDBCDriver](500164) Error initialized or created transport for authentication: null.
// */
object MainOntopSesameWithImpala extends App {

  val logger = LoggerFactory.getLogger(this.getClass)

  val db_driver = "com.cloudera.impala.jdbc41.Driver"
  val db_name = "opendata.roma_o_incidenti_d_stradali"
  Class.forName(db_driver)

  val dsn = "jdbc:impala://slave4.platform.daf.gov.it:21050;SSL=1;SSLKeyStore=C:/Users/Al.Serafini/awavedev/progetti/DAF/ssl_impala/master-impala.jks;SSLKeyStorePwd=Ahdai5th;AuthMech=3;CAIssuedCertNamesMismatch=1"
  val usr = "aserafini"
  val pwd = "openD4ti"

  val db_conn = DriverManager.getConnection(dsn, usr, pwd)
  db_conn.close()

  // -------------------------------------------------------------------------------

  val r2rmlFile = new File("src/test/resources/r2rml/poc_anpr_comuni.r2rml.ttl").getAbsoluteFile;

  val r2rmlModel = loadR2RML(r2rmlFile.toString());

  val owlOntology = createOWLOntology() //loadOWLOntology(owlFile);

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

  def loadR2RML(r2rmlFile: String): Model = {
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