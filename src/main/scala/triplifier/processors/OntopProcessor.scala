package triplifier.processors

import com.typesafe.config.Config
import org.slf4j.LoggerFactory
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences
import org.openrdf.model.ValueFactory
import java.io.OutputStream
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants
import it.unibz.inf.ontop.sesame.SesameVirtualRepo
import java.time.LocalDateTime
import org.openrdf.repository.RepositoryConnection
import info.aduna.iteration.Iterations
import org.openrdf.rio.RDFFormat
import org.openrdf.rio.Rio
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import java.nio.file.Paths
import java.nio.file.Files
import scala.util.Random
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.openrdf.model.Statement
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.apibinding.OWLManager
import org.openrdf.model.Model
import org.openrdf.model.impl.LinkedHashModel
import org.openrdf.rio.RDFParser
import org.openrdf.rio.helpers.StatementCollector
import java.io.ByteArrayInputStream
import com.typesafe.config.ConfigFactory
import org.openrdf.rio.WriterConfig
import com.typesafe.config.ConfigParseOptions
import com.typesafe.config.ConfigSyntax
import scala.concurrent.Future
import scala.util.Try
import org.eclipse.rdf4j.model.ModelFactory

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.util.Success
import scala.util.Failure

object OntopProcessor {

  val options = ConfigParseOptions.defaults().setAllowMissing(true).setSyntax(ConfigSyntax.CONF)

  def parse(conf: String) = new OntopProcessor(ConfigFactory.parseString(conf, options).resolve())

  def apply(conf: Config) = new OntopProcessor(conf)

  def sqlite = {

    val _conf = ConfigFactory.parseString("""
    
      jdbc.driver = "org.sqlite.JDBC"
      jdbc.dsn = "jdbc:sqlite:C:/Users/Al.Serafini/repos/DAF/db/test_comuni.db"
      jdbc.user = "aserafini"
      jdbc.password = "openD4ti"
      
    """)

    new OntopProcessor(_conf)

  }

  def impala = {

    val _conf = ConfigFactory.parseString("""

      jdbc.driver = "com.cloudera.impala.jdbc41.Driver"
      jdbc.dsn = "jdbc:impala://slave4.platform.daf.gov.it:21050;SSL=1;SSLKeyStore=C:/Users/Al.Serafini/awavedev/progetti/DAF/ssl_impala/master-impala.jks;SSLKeyStorePwd=Ahdai5th;AuthMech=3;CAIssuedCertNamesMismatch=1"
      jdbc.user = "aserafini"
      jdbc.password = "openD4ti"
      
    """)

    new OntopProcessor(_conf)

  }

}

/**
 *
 * REFACTORIZATION
 *
 * [work-in-progress]
 *
 */
class OntopProcessor(config: Config) extends RDFProcessor {

  import scala.concurrent.ExecutionContext.Implicits.global

  val conf = config.resolve()

  val jdbc_driver = conf.getString("jdbc.driver")
  val jdbc_dsn = conf.getString("jdbc.dsn")
  val jdbc_usr = conf.getString("jdbc.user")
  val jdbc_pwd = conf.getString("jdbc.password")

  val db_name = "daf_comuni_test"
  val repo_name = "test_repo"

  val logger = LoggerFactory.getLogger(this.getClass)

  Class.forName(conf.getString("jdbc.driver"))

  // CHECK CONNECTION alive?

  // prepare ontology
  val owlOntology = createOWLOntology()

  // TODO: automation of re-creation of test db
  val preferences = new QuestPreferences()
  preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL)
  preferences.setCurrentValueOf(QuestPreferences.DBNAME, db_name)
  preferences.setCurrentValueOf(QuestPreferences.JDBC_DRIVER, jdbc_driver)
  preferences.setCurrentValueOf(QuestPreferences.JDBC_URL, jdbc_dsn)
  preferences.setCurrentValueOf(QuestPreferences.DBUSER, jdbc_usr)
  preferences.setCurrentValueOf(QuestPreferences.DBPASSWORD, jdbc_pwd)

  //  preferences.setCurrentValueOf("org.obda.owlreformulationplatform.queryingAnnotationsInOntology", "false")
  preferences.setCurrentValueOf(QuestPreferences.ANNOTATIONS_IN_ONTO, "false")
  preferences.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED)
  preferences.setCurrentValueOf(QuestPreferences.KEEP_ALIVE, "true")

  // needed for Impala SQL
  preferences.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, "false") // AVOID REPLACE !!

  var vf: ValueFactory = null

  def guessBaseURI() = if (config.hasPath("r2rml.baseURI")) config.getString("r2rml.baseURI") else "https://test/"

  def process(r2rml: String): Try[Seq[Statement]] = Try {

    val baseURI = guessBaseURI

    val start_time = LocalDateTime.now()

    val r2rmlModel = loadTurtle(r2rml, baseURI).get

    // CHECK namespaces: val namespaces = r2rmlModel.getNamespaces

    val triplesMaps = this.triplesMaps(r2rmlModel).get

    logger.info("\nRDF mapping, using TripleMap definitions:")
    logger.info(triplesMaps.mkString("\n"))

    val repo = new SesameVirtualRepo(repo_name, owlOntology, r2rmlModel, preferences)
    // IDEA: notification sail instead of Iterations of statements

    if (!repo.isInitialized()) repo.initialize()

    val conn: RepositoryConnection = repo.getConnection()

    vf = conn.getValueFactory

    val statements = Iterations.asList(conn.getStatements(null, null, null, true)).toStream

    val size = statements.size

    val end_time = LocalDateTime.now()

    val processing_time = Duration.create(end_time.getNano - start_time.getNano, TimeUnit.NANOSECONDS).toCoarsest

    logger.info(s"${size} triples created in ${processing_time}\n\n")

    conn.close()

    if (repo.isInitialized()) repo.shutDown()

    statements

  }

  /*
   * This method is useful to create a dump of the data, on a choosen outputstream,
   * which can be a file, a response writer, and so on.
   *
   * TODO: define a decorator for writing on string
   */
  def dump(r2rml_list: Seq[String])(metadata: Option[String])(rdf_data: Option[String])(out: OutputStream, rdf_format: RDFFormat = RDFFormat.NTRIPLES) {

    // CHECK val settings = new WriterConfig for formatting, pretty-print etc

    val metadata_statements: Seq[Statement] = loadTurtle(
      metadata.getOrElse(""),
      guessBaseURI)
      .getOrElse(new LinkedHashModel) // workaround for missing metadata
      .toStream

    val dump_statements = r2rml_list
      .par // CHECK if works
      .flatMap { r2rml => process(r2rml).get }
      .toStream
      .sortWith {
        // this is an hack for improving readability of serialized output, for example for turtles
        (st1, st2) =>
          val test1 = s"${st1.getContext}${st1.getSubject}${st1.getPredicate}"
          val test2 = s"${st2.getContext}${st2.getSubject}${st2.getPredicate}"
          test1.compareTo(test2) < 0
      }
      .distinct // NOTE: distinct is needed due to a bug in this version of ontop!

    // CHECK: avoid saving head of streams here!
    val statements = (metadata_statements ++ dump_statements)

    // TODO: improve pretty print
    Rio.write(statements, out, rdf_format)

  }

  /* this is an helper method, to preview a sample of the generated data */
  def previewDump(rdfFileName: String, offset: Int = -1, limit: Int = -1): String = {

    // VERIFY: default values for collections.slice()
    val from = if (offset > 0) offset else Random.nextInt(100)
    val to = if (limit > 0) offset else Random.nextInt(500)

    Files.readAllLines(Paths.get(rdfFileName).toAbsolutePath().normalize())
      .toStream
      .zipWithIndex.map(_.swap)
      .map(x => s"${x._1}\t${x._2}")
      .slice(from, to).mkString("\n")

  }

  /* the creation of an OWLOntology is required by the processor, so we need to create at least an empty one */
  private def createOWLOntology(): OWLOntology = {

    val owlManager = OWLManager.createOWLOntologyManager()
    owlManager.createOntology()

  }

  /* this method is used for parameters injection in mappings or static metadata */
  def injectParameters(
    content:    String,
    parameters: Config = ConfigFactory.empty()): String = {

    logger.debug(s"\n\n using parameters: ${parameters}")

    content
      .split("\n")
      .toStream
      .map { line =>
        val subs = parameters.entrySet().toList
        var txt = line
        subs.map { s =>
          txt = txt.replace(s"{${s.getKey}}", s.getValue.unwrapped().toString())
        }
        txt
      }
      .mkString("\n")

  }

  /* this can be used to create a Model from the R2RML mapping */
  def loadTurtle(
    rdf_content: String,
    baseURI:     String = "test://memory/"): Try[Model] = Try {

    // parameters interpolation
    val rdf_content_with_parameters = if (config.hasPath("parameters"))
      injectParameters(rdf_content, config.getConfig("parameters")) // TODO: add check for parameters
    else
      rdf_content

    val rdfParser: RDFParser = Rio.createParser(RDFFormat.TURTLE)
    val r2rmlModel: Model = new LinkedHashModel()
    val collector = new StatementCollector(r2rmlModel)
    rdfParser.setRDFHandler(collector)
    val bais = new ByteArrayInputStream(rdf_content_with_parameters.getBytes)
    rdfParser.parse(bais, baseURI)
    bais.close()
    r2rmlModel

  }

  /* gets a list of the available triplesMap */
  def triplesMaps(r2rmlModel: Model): Try[Seq[String]] = Try {

    val rdf_type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
    val triplesMapClass = "http://www.w3.org/ns/r2rml#TriplesMapClass"

    r2rmlModel.iterator().toStream
      .filter { st =>
        val prp = st.getPredicate.stringValue()
        val obj = st.getObject.stringValue()
        prp.equals(rdf_type) && obj.equals(triplesMapClass)
      }.map(_.getSubject.stringValue())

  }

}