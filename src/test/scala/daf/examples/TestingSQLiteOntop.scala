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
import it.unibz.inf.ontop.owlapi.bootstrapping.DirectMappingBootstrapper
import org.openrdf.rio.turtle.TurtleWriter
import org.openrdf.rio.WriterConfig
import org.openrdf.rio.helpers.BasicWriterSettings
import java.lang.{ Boolean => JBoolean }
import org.openrdf.query.resultio.BasicQueryWriterSettings
import org.openrdf.rio.helpers.XMLWriterSettings
import info.aduna.iteration.Iterations

/*
 * REVIEW: 
 */
object TestingSQLiteOntop extends App {

  val logger = LoggerFactory.getLogger(this.getClass)

  val db_driver = "org.sqlite.JDBC"
  val db_name = "daf_comuni_test"
  Class.forName(db_driver)

  val dsn = "jdbc:sqlite:C:/Users/Al.Serafini/repos/DAF/db/test_comuni.db"
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
  //  dm_onto.saveOntology(new TurtleDocumentFormat, System.err)

  // TODO: save R2RML ?

  // -------------------------------------------------------------------------------

  val dump_file = "target/EXPORT/poc_regioni.ttl"
  val r2rmlFile = new File("src/test/resources/r2rml/anpr/poc_regioni.r2rml.ttl").getAbsoluteFile;

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

  println("\n\n#### RDF mapping - START")

  val start_time = LocalDateTime.now()

  val repo = new SesameVirtualRepo("test_repo", owlOntology, r2rmlModel, preferences);

  repo.initialize();

  val conn: RepositoryConnection = repo.getConnection()

  val statements = Iterations.asList(conn.getStatements(null, null, null, true))
    .toStream
    .distinct
    // .sortBy(_.getSubject.stringValue())
    .sortWith((st1, st2) => st1.toString().compareTo(st2.toString()) < 0)

  val output_file = new File(dump_file).getAbsoluteFile
  if (!output_file.getParentFile.exists()) output_file.getParentFile.mkdirs()
  val fos = new FileOutputStream(output_file)
  val format = Rio.getParserFormatForFileName(output_file.getName)

  //  Rio.write(statements, fos, format)
  //  Rio.write(statements, fos, format, config)

  // CUSTOM TURTLE WRITER (PRETTY-PRINT)
  val rdf_writer = new TurtleWriter(fos) {

    val config = new WriterConfig
    config.set[JBoolean](BasicWriterSettings.PRETTY_PRINT, true)
    config.set[JBoolean](BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL, true)
    config.set[JBoolean](BasicWriterSettings.RDF_LANGSTRING_TO_LANG_LITERAL, true)
    config.set[JBoolean](BasicQueryWriterSettings.ADD_SESAME_QNAME, false)
    config.set[JBoolean](XMLWriterSettings.INCLUDE_ROOT_RDF_TAG, true)
    config.set[JBoolean](BasicWriterSettings.RDF_LANGSTRING_TO_LANG_LITERAL, true)
    config.set[JBoolean](BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL, true)

    this.handleNamespace("skos", "http://www.w3.org/2004/02/skos/core#")
    this.handleNamespace("l0", "https://w3id.org/italia/onto/l0/")
    this.handleNamespace("clvapit", "https://w3id.org/italia/onto/CLV/")
    this.handleNamespace("countries", "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/countries/")
    this.handleNamespace("regions", "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/regions/")
    this.handleNamespace("provinces", "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/provinces/")
    this.handleNamespace("cities", "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/cities/")
    this.handleNamespace("identifiers", "https://w3id.org/italia/controlled-vocabulary/identifiers/")

    this.setWriterConfig(config)

  }
  Rio.write(statements, rdf_writer)

  fos.close()

  val end_time = LocalDateTime.now()

  val processing_time = Duration.create(end_time.getNano - start_time.getNano, TimeUnit.NANOSECONDS).toCoarsest

  logger.info(s"the file ${output_file.getName} was created in ${processing_time}")

  // releasing connection
  conn.close()

  repo.shutDown()

  val dump = Files.readAllLines(output_file.toPath())
    .zipWithIndex.map(_.swap)
    .map(x => s"${x._1}\t${x._2}")
    .slice(Random.nextInt(100), Random.nextInt(500)).mkString("\n")
  println("\n\n\n\nRDF DUMP")
  println(dump)

  println("#### RDF mapping - STOP")

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
    rdfParser.parse(fis, "test://mapping");
    r2rmlModel
  }

  def loadSPARQL(sparqlFile: String): String = {
    val src = Source.fromFile(sparqlFile, "UTF-8")
    val txt = src.getLines().mkString("\n")
    src.close()
    txt
  }
}



