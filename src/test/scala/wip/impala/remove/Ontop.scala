package wip.impala.remove

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import it.unibz.inf.ontop.owlapi.bootstrapping.DirectMappingBootstrapper
import org.semanticweb.owlapi.model.OWLOntology
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants
import org.openrdf.model.Statement
import java.time.LocalDateTime
import java.nio.file.Paths
import it.unibz.inf.ontop.sesame.SesameVirtualRepo
import org.openrdf.repository.RepositoryConnection
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import java.io.OutputStream
import org.openrdf.rio.RDFFormat
import org.openrdf.rio.Rio

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.io.Source
import java.io.ByteArrayInputStream
import org.openrdf.model.impl.LinkedHashModel
import org.openrdf.model.Model
import org.openrdf.rio.RDFParser
import org.openrdf.rio.helpers.StatementCollector
import org.semanticweb.owlapi.apibinding.OWLManager
import scala.util.Random
import java.nio.file.Files
import java.io.FileOutputStream
import info.aduna.iteration.Iterations
import daf.poc.rdf.RDF4JPrettyTurtleWriter

object Ontop {

  // TODO: handle multiple mappings in a row (in order to be able to divide them!)

  // TODO: externalize configs!
  val parameters = ConfigFactory.parseString("""
    
    # impala JDBC config
    impala {
      jdbc {
        host = "slave4.platform.daf.gov.it"
        port = 21050
        dsn = "jdbc:impala://"${impala.jdbc.host}":"${impala.jdbc.port}";SSL=1;SSLKeyStore="${impala.jks_file}";SSLKeyStorePwd=Ahdai5th;AuthMech=3;CAIssuedCertNamesMismatch=1"
        driver = "com.cloudera.impala.jdbc41.Driver"
        username = "aserafini"
        password = "openD4ti"
      }
      jks_file = "./ssl_impala/master-impala.jks"
    }
    
    sqlite.jdbc {
      host = "slave4.platform.daf.gov.it"
      port = 21050
      dsn = "jdbc:sqlite:C:/Users/Al.Serafini/repos/DAF/db/test_comuni.db"
      driver = "org.sqlite.JDBC"
      username = "aserafini"
      password = "openD4ti"
    }
    
    jdbc = ${impala.jdbc}
    
    repository.name = "test_anpr_comuni"
    db.name = "opendata"
    
    # REVIEW
    vocabularies.base = "https://w3id.org/italia/controlled-vocabulary"
    
    ontologies.base = "https://w3id.org/italia/onto"
    
  """).resolve()

  val logger = LoggerFactory.getLogger(this.getClass)

  // ---- CONFIGS ----------------------------------------------------

  val db_driver = parameters.getString("jdbc.driver")
  Class.forName(db_driver)

  val dsn = parameters.getString("jdbc.dsn")
  val usr = parameters.getString("jdbc.username")
  val pwd = parameters.getString("jdbc.password")
  val base_uri = parameters.getString("ontologies.base")

  val dm_boot = new DirectMappingBootstrapper(
    base_uri,
    dsn,
    usr, pwd,
    db_driver)

  val dm_onto: OWLOntology = dm_boot.getOntology

  // CHECK  dm_onto.saveOntology(new TurtleDocumentFormat, System.err)

  // CHECK: st.executeUpdate("""USE opendata""")

  // CHECK: save R2RML ?

  // REVIEW: QuestPreferences
  // TODO: automation of re-creation of test db
  val preferences = new QuestPreferences()
  preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL)
  preferences.setCurrentValueOf(QuestPreferences.DBNAME, "PROVA") //parameters.getString("db.name")) // REVIEW: maybe this should be used in process()
  preferences.setCurrentValueOf(QuestPreferences.JDBC_DRIVER, db_driver)
  preferences.setCurrentValueOf(QuestPreferences.JDBC_URL, dsn)
  preferences.setCurrentValueOf(QuestPreferences.DBUSER, usr)
  preferences.setCurrentValueOf(QuestPreferences.DBPASSWORD, pwd)
  preferences.setCurrentValueOf(QuestPreferences.ANNOTATIONS_IN_ONTO, "false")
  //  preferences.setCurrentValueOf("org.obda.owlreformulationplatform.queryingAnnotationsInOntology", "false")

  // HACK: needed for Impala SQL!!
  preferences.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, "false")

  // ---- REVIEW ----

  //REFORMULATION_TECHNIQUE
  //DBTYPE
  //OBTAIN_FROM_ONTOLOGY
  //OBTAIN_FROM_MAPPINGS
  //OPTIMIZE_EQUIVALENCES
  //SAME_AS
  //BASE_IRI
  //OBTAIN_FULL_METADATA
  //DISTINCT_RESULTSET
  //REWRITE
  //STORAGE_LOCATION
  //PRINT_KEYS
  //// Tomcat connection pool properties
  //MAX_POOL_SIZE
  //INIT_POOL_SIZE
  //REMOVE_ABANDONED
  //ABANDONED_TIMEOUT
  //KEEP_ALIVE

  // ---- REVIEW ----

  // ---- CONFIGS ----------------------------------------------------

  /**
   * This method applies the choosen R2RML mapping file, and process the data source,
   * in order to produce a dump of RDF data.
   *
   * IDEA: In case we need to add extra triples to the dump, we can pass them using extra_statements.
   *
   * Using it as a curryied function, is possible to apply different side-effect functions,
   * designed to save the dump on file, to publish it, and so on
   */
  def processR2RML[R](
    r2rmlFileName:    String,
    extra_statements: Seq[Statement] = List())(implicit dump_action: (Seq[Statement]) => R) = {

    val start_time = LocalDateTime.now()

    val r2rmlFile = Paths.get(r2rmlFileName).toAbsolutePath().normalize()

    val r2rmlModel = loadR2RML(r2rmlFile.toString())

    val owlOntology = createOWLOntology() // HACK: start with an empty ontology

    val repo = new SesameVirtualRepo(
      parameters.getString("repository.name"),
      owlOntology, r2rmlModel,
      preferences)

    // in this case, the repository is created
    if (!repo.isInitialized()) repo.initialize()

    val conn: RepositoryConnection = repo.getConnection()

    // handles all the data produced by RDF processor, in some way defined externally
    val _statements = dump_action {

      (extra_statements ++ Iterations.asList(conn.getStatements(null, null, null, true)))
        .toList // REVIEW: Stream
        .distinct
      //   REVIEW     .sortWith((st1, st2) => st1.toString().compareTo(st2.toString()) < 0)

    }

    // releasing connection
    conn.close()

    if (repo.isInitialized()) repo.shutDown()

    val end_time = LocalDateTime.now()
    val processing_time = Duration.create(end_time.getNano - start_time.getNano, TimeUnit.NANOSECONDS).toCoarsest

    logger.info(s"RDF data created using ${r2rmlFileName} in ${processing_time}")

    _statements

  }

  def writeToOutputStream(_statements: Seq[Statement], out: OutputStream, format: RDFFormat = RDFFormat.NTRIPLES): Seq[Statement] = {

    if (format.equals(RDFFormat.TURTLE)) {
      new RDF4JPrettyTurtleWriter(out)
    } else {
      val rdf_writer = Rio.createWriter(format, out)
      Rio.write(_statements.toList, rdf_writer)
    }

    _statements

  }

  def writeDump(_statements: Seq[Statement], dump_file: String): Seq[Statement] = {

    val output_file = Paths.get(dump_file).toAbsolutePath().normalize().toFile()
    if (!output_file.getParentFile.exists()) output_file.getParentFile.mkdirs()

    val format = Rio.getParserFormatForFileName(output_file.getName)
    val fos = new FileOutputStream(output_file)

    // TODO: extends to other formats!
    // CUSTOM TURTLE WRITER (PRETTY-PRINT)
    val rdf_writer = new RDF4JPrettyTurtleWriter(fos)

    Rio.write(_statements, rdf_writer)

    fos.close()

    _statements

  }

  // creates a preview of the RDF dump
  def previewDump(dumpFileName: String): String = {

    val dumpFile = Paths.get(dumpFileName).toAbsolutePath().normalize()

    Files.readAllLines(dumpFile)
      .zipWithIndex.map(_.swap)
      .map(x => s"${x._1}\t${x._2}")
      .slice(Random.nextInt(100), Random.nextInt(500)).mkString("\n")

  }

  // creates a new OWLOntology object
  def createOWLOntology(): OWLOntology = {
    val owlManager = OWLManager.createOWLOntologyManager()
    owlManager.createOntology()
  }

  // creates a new OWLOntology object
  def loadOWLOntology(owlFileName: String): OWLOntology = {
    val owlManager = OWLManager.createOWLOntologyManager()
    val owlFile = Paths.get(owlFileName).toAbsolutePath().normalize().toFile()
    owlManager.loadOntologyFromOntologyDocument(owlFile)
  }

  /*
   * this methods helps injecting parameters inside a file (typically a mapping file!)
   */
  def injectParameters(content: String, ps: Map[String, Object]): String = {
    var txt = content
    if (txt.contains("{") && txt.contains("}"))
      ps.foreach { kv => txt = txt.replace(s"""{${kv._1}}""", kv._2.toString()) }
    txt
  }

  def loadR2RML(r2rmlFileName: String): Model = {

    val r2rmlFile = Paths.get(r2rmlFileName).toAbsolutePath().normalize().toFile()

    val rdfParser: RDFParser = Rio.createParser(RDFFormat.TURTLE)
    val r2rmlModel: Model = new LinkedHashModel()
    val collector = new StatementCollector(r2rmlModel)
    rdfParser.setRDFHandler(collector)

    // prepare parameters
    val ps = parameters.entrySet().toList
      .map(e => (e.getKey, e.getValue.unwrapped()))
      .toMap

    // inject parameters in mapping file
    val src = Source.fromFile(r2rmlFile)("UTF-8")
    val r2rml_content = injectParameters(src.getLines().mkString("\n"), ps)
    src.close()

    val bais = new ByteArrayInputStream(r2rml_content.getBytes)
    rdfParser.parse(bais, s"r2rml://mapping/${r2rmlFile.getName}")
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