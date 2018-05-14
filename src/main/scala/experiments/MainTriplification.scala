package experiments

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.seqAsJavaList
import scala.concurrent.duration.Duration
import scala.io.Source
import scala.util.Random

import org.openrdf.model.Model
import org.openrdf.model.impl.LinkedHashModel
import org.openrdf.rio.RDFFormat
import org.openrdf.rio.RDFParser
import org.openrdf.rio.Rio
import org.openrdf.rio.helpers.StatementCollector
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLOntology
import org.slf4j.LoggerFactory

import info.aduna.iteration.Iterations
import it.unibz.inf.ontop.owlapi.bootstrapping.DirectMappingBootstrapper
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences
import it.unibz.inf.ontop.sesame.RepositoryConnection
import it.unibz.inf.ontop.sesame.SesameVirtualRepo
import org.openrdf.model.Statement
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.ByteArrayInputStream

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object MainTriplification extends App {

  val dump_file = "target/EXPORT/test_regioni_v02.ttl"
  val r2rml_file = "src/test/resources/r2rml/test_regioni_v02.r2rml2.ttl"

  val ontop = Ontop

  ontop.triplify_to_file(r2rml_file, dump_file)

  val dump = ontop.readDump(dump_file)
  println(dump)

}

object Ontop {

  val parameters = ConfigFactory.parseString("""
    
    repository.name = "test_anpr_comuni"
    vocabularies.base = "https://w3id.org/italia/controlled-vocabulary"
    
  """).resolve()

  val logger = LoggerFactory.getLogger(this.getClass)

  // ---- CONFIGS ----------------------------------------------------

  val db_driver = "org.sqlite.JDBC"
  val db_name = "daf_comuni_test"
  Class.forName(db_driver)

  val dsn = "jdbc:sqlite:C:/Users/Al.Serafini/repos/DAF/db/test_comuni.db"
  val usr = "aserafini"
  val pwd = "openD4ti"

  val dm_boot = new DirectMappingBootstrapper(
    "test://w3id.org/italia/onto/",
    dsn,
    usr, pwd,
    db_driver)

  val dm_onto: OWLOntology = dm_boot.getOntology

  // CHECK  dm_onto.saveOntology(new TurtleDocumentFormat, System.err)

  // CHECK: save R2RML ?

  // TODO: automation of re-creation of test db
  val preferences = new QuestPreferences()
  preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL)
  preferences.setCurrentValueOf(QuestPreferences.DBNAME, db_name)
  preferences.setCurrentValueOf(QuestPreferences.JDBC_DRIVER, db_driver)
  preferences.setCurrentValueOf(QuestPreferences.JDBC_URL, dsn)
  preferences.setCurrentValueOf(QuestPreferences.DBUSER, usr)
  preferences.setCurrentValueOf(QuestPreferences.DBPASSWORD, pwd)
  preferences.setCurrentValueOf("org.obda.owlreformulationplatform.queryingAnnotationsInOntology", "false")
  // HACK: needed for Impala SQL!!
  preferences.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, "false")

  // ---- CONFIGS ----------------------------------------------------

  def writeDump(statements: Seq[Statement], r2rmlFileName: String, dump_file: String) {

  }

  def triplify_to_file(r2rmlFileName: String, dump_file: String) {

    val start_time = LocalDateTime.now()

    val r2rmlFile = Paths.get(r2rmlFileName).toAbsolutePath().normalize()

    val r2rmlModel = loadR2RML(r2rmlFile.toString())

    val owlOntology = createOWLOntology() // HACK: start with an empty ontlogy

    val repo = new SesameVirtualRepo(
      parameters.getString("repository.name"),
      owlOntology, r2rmlModel,
      preferences)

    if (!repo.isInitialized()) repo.initialize()

    val conn: RepositoryConnection = repo.getConnection()

    val statements: Seq[Statement] = Iterations.asList(conn.getStatements(null, null, null, true))
      .toStream
      .distinct
      .sortWith((st1, st2) => st1.toString().compareTo(st2.toString()) < 0)

    val output_file = Paths.get(dump_file).toAbsolutePath().normalize().toFile()

    if (!output_file.getParentFile.exists()) output_file.getParentFile.mkdirs()

    val format = Rio.getParserFormatForFileName(output_file.getName)

    val fos = new FileOutputStream(output_file)

    // CUSTOM TURTLE WRITER (PRETTY-PRINT)
    val rdf_writer = new RDF4JPrettyTurtleWriter(fos)

    Rio.write(statements, rdf_writer)

    fos.close()

    val end_time = LocalDateTime.now()

    val processing_time = Duration.create(end_time.getNano - start_time.getNano, TimeUnit.NANOSECONDS).toCoarsest

    logger.info(s"the file ${output_file.getName} was created in ${processing_time}")

    // releasing connection
    conn.close()

    if (repo.isInitialized()) repo.shutDown()

  }

  def readDump(dumpFileName: String) = {
    Files.readAllLines(Paths.get(dumpFileName))
      .zipWithIndex.map(_.swap)
      .map(x => s"${x._1}\t${x._2}")
      .slice(Random.nextInt(100), Random.nextInt(500)).mkString("\n")

  }

  def createOWLOntology(): OWLOntology = {
    val owlManager = OWLManager.createOWLOntologyManager()
    owlManager.createOntology()
  }

  def loadOWLOntology(owlFile: String): OWLOntology = {
    val owlManager = OWLManager.createOWLOntologyManager()
    owlManager.loadOntologyFromOntologyDocument(new File(owlFile))
  }

  def injectParameters(content: String, ps: Map[String, Object]): String = {
    var txt = content
    if (txt.contains("{") && txt.contains("}"))
      ps.foreach { kv => txt = txt.replace(s"""{${kv._1}}""", kv._2.toString()) }
    txt
  }

  def loadR2RML(r2rmlFile: String): Model = {

    val rdfParser: RDFParser = Rio.createParser(RDFFormat.TURTLE)
    val r2rmlModel: Model = new LinkedHashModel()
    val collector = new StatementCollector(r2rmlModel)
    rdfParser.setRDFHandler(collector)

    // prepare parameters
    val ps = parameters.entrySet().toList
      .map(e => (e.getKey, e.getValue.unwrapped()))
      .toMap

    // inject parameters in mapping file
    val src = Source.fromFile(new File(r2rmlFile))("UTF-8")
    val r2rml_content = injectParameters(src.getLines().mkString("\n"), ps)
    src.close()

    val bais = new ByteArrayInputStream(r2rml_content.getBytes)
    rdfParser.parse(bais, "test://mapping")
    bais.close()

    r2rmlModel
  }

  def loadSPARQL(sparqlFile: String): String = {
    val src = Source.fromFile(sparqlFile, "UTF-8")
    val txt = src.getLines().mkString("\n")
    src.close()
    txt
  }

}


