package POC.comuni

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

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.util.Random
import org.sqlite.Function
import it.unibz.inf.ontop.r2rml.R2RMLManager
import org.semanticweb.owlapi.model.OWLDocumentFormat
import org.semanticweb.owlapi.formats.TurtleDocumentFormat
import it.unibz.inf.ontop.model.OBDAModel

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import it.unibz.inf.ontop.r2rml.R2RMLParser
import it.unibz.inf.ontop.r2rml.R2RMLReader
import it.unibz.inf.ontop.r2rml.OBDAMappingTransformer
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.openrdf.model.impl.TreeModel
import it.unibz.inf.ontop.owlapi.bootstrapping.DirectMappingBootstrapper
import it.unibz.inf.ontop.r2rml.R2RMLWriter
import eu.optique.api.mapping.impl.R2RMLUtil
import org.openrdf.repository.sparql.SPARQLRepository
import scala.util.Try

object MainOntopSQlite extends App {

  val logger = LoggerFactory.getLogger(this.getClass)

  val db_driver = "org.sqlite.JDBC"
  val db_name = "daf_comuni_test"
  Class.forName(db_driver)

  val dsn = "jdbc:sqlite:C:/Users/a.mauro/IdeaProjects/daf-triplifier/db/test_comuni.db"
  val usr = "aserafini"
  val pwd = "openD4ti"

  // -------------------------------------------------------------------------------
  // TESTING DirectMapping

  val dm_boot = new DirectMappingBootstrapper(
    "test://w3id.org/italia/onto/",
    dsn,
    usr, pwd,
    db_driver)

  val dm_onto: OWLOntology = dm_boot.getOntology
  dm_onto.saveOntology(new TurtleDocumentFormat, System.err)

  // TODO: save R2RML ?

  // -------------------------------------------------------------------------------

  val r2rmlFile = new File("src/test/resources/r2rml.old/ex_01.r2rml.ttl").getAbsoluteFile;

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
  val output_file = new File("target/EXPORT/testing_rdf.sqlite.nt").getAbsoluteFile
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
    .zipWithIndex.map(_.swap)
    .slice(Random.nextInt(100), Random.nextInt(500)).mkString("\n")
  println("\n\n\n\nRDF DUMP")
  println(dump)

  //  ---- LOAD TRIPLES into TRIPLESTORE ----
  try {
    val baseURI = "http://w3id.org/italia/data/"
    val context = s"http://w3id.org/italia/dataset/${r2rmlFile.getName}"
    val endpoint = "http://localhost:9999/blazegraph/sparql"
    Triplestore.load(endpoint, r2rmlFile, context)
  } catch {
    case err: Exception => System.err.println("\n\nWARNING: cannot load data!\n" + err.getMessage)
  }

  // TODO: parameters injection
  // TODO: add static RDF to dump (for metadata)

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
    rdfParser.parse(fis, "test://example.org");
    r2rmlModel
  }

  def loadSPARQL(sparqlFile: String): String = {
    val src = Source.fromFile(sparqlFile, "UTF-8")
    val txt = src.getLines().mkString("\n")
    src.close()
    txt
  }
}

object Triplestore {

  def apply(endpoint: String) = new Triplestore(endpoint, endpoint)

  def load(endpoint: String, r2rmlFile: File, context: String) = new Triplestore(endpoint, endpoint).loadGraph(r2rmlFile, context, "", true)

}

class Triplestore(endpointQuery: String, endpointUpdate: String) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def loadGraph(
    r2rmlFile: File,
    context:   String,
    baseURI:   String  = "",
    clear:     Boolean = true) {

    val triplestore = new SPARQLRepository(endpointQuery, endpointUpdate)
    triplestore.initialize()

    val tconn = triplestore.getConnection

    val rdf_dump = s"${r2rmlFile.toURI().toURL()}"
    logger.debug(s"loading rdf dump: ${r2rmlFile}")

    if (clear) {

      tconn.begin()
      val q_drop = QUERY.DROP(context)
      tconn.prepareUpdate(QueryLanguage.SPARQL, q_drop, baseURI).execute()
      logger.debug("deleting all triples for context <${context}>")
      logger.debug(s"SPARQL>\n${q_drop}")
      tconn.commit()

    }

    {
      tconn.begin()
      val q_load = QUERY.LOAD(rdf_dump, context)
      tconn.prepareUpdate(QueryLanguage.SPARQL, q_load, baseURI).execute()
      logger.debug(s"loading data into context <${context}> from ${rdf_dump}")
      logger.debug(s"SPARQL>\n${q_load}")
      tconn.commit()
    }

    tconn.close()

    triplestore.shutDown()

  }

  object QUERY {

    def DROP(context: String) = s"""
      DROP GRAPH <${context}> 
      ;
    """.replaceAll("\\s+", " ").trim()

    def LOAD(rdf_dump: String, context: String) = s"""
      LOAD <${rdf_dump}>
      INTO GRAPH <${context}>
      ;
    """.replaceAll("\\s+", " ").trim()

  }

}

