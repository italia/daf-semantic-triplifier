package other
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.sesame.RepositoryConnection;
import it.unibz.inf.ontop.sesame.SesameVirtualRepo;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.Binding;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import scala.io.Source
import java.sql.DriverManager
import scala.collection.mutable.ListBuffer
import org.openrdf.model.Statement
import org.openrdf.rio.Rio

import java.io.FileOutputStream
import java.text.SimpleDateFormat
import scala.concurrent.duration.Duration
import java.util.Date
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.charset.Charset

object MainOntopSQlite extends App {

  val logger = LoggerFactory.getLogger(this.getClass)

  val db_driver = "org.sqlite.JDBC"
  val db_name = "daf_comuni_test"
  Class.forName(db_driver)

  val dsn = "jdbc:sqlite:C:/Users/Al.Serafini/repos/DAF/db/test_comuni.db"
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

  val statements = conn.getStatements(null, null, null, false).asList()
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

  System.exit(0)

  // ---------------------------------------

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