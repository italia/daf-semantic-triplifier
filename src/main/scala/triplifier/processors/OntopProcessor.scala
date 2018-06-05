

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
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.openrdf.rio.WriterConfig
import com.typesafe.config.ConfigParseOptions
import com.typesafe.config.ConfigSyntax

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
class OntopProcessor(config: Config) {

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

  def process(r2rml: String): Seq[Statement] = {

    val start_time = LocalDateTime.now()

    val r2rmlModel = loadR2RMLString(r2rml) // TODO: manage baseURI by configuration!

    // CHECK namespaces
    val namespaces = r2rmlModel.getNamespaces

    val triplesMaps = this.triplesMaps(r2rmlModel)

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

  def dump(r2rml: String, out: OutputStream, rdf_format: RDFFormat) {
    dump(List(r2rml), out, rdf_format)
  }

  /*
   * TODO:
   * 	+ dump to file
   */
  def dump(r2rml_list: Seq[String], out: OutputStream, rdf_format: RDFFormat = RDFFormat.NTRIPLES) {

    val statements = r2rml_list
      .par // CHECK if works
      .flatMap { r2rml => process(r2rml) }
      .toStream
      .sortWith {
        (st1, st2) =>
          val test1 = s"${st1.getContext}${st1.getSubject}${st1.getPredicate}"
          val test2 = s"${st2.getContext}${st2.getSubject}${st2.getPredicate}"
          test1.compareTo(test2) < 0
      }.distinct // NOTE: distinct is needed due to a bug in this version of ontop!

    // CHECK
    val settings = new WriterConfig

    // TODO: pretty print
    Rio.write(statements, out, rdf_format)

  }

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

  private def createOWLOntology(): OWLOntology = {

    val owlManager = OWLManager.createOWLOntologyManager()
    owlManager.createOntology()

  }

  def loadR2RMLString(r2rml: String, baseURI: String = "test://memory/"): Model = {

    val rdfParser: RDFParser = Rio.createParser(RDFFormat.TURTLE)
    val r2rmlModel: Model = new LinkedHashModel()
    val collector = new StatementCollector(r2rmlModel)
    rdfParser.setRDFHandler(collector)
    val bais = new ByteArrayInputStream(r2rml.getBytes)
    rdfParser.parse(bais, baseURI)
    bais.close()
    r2rmlModel

  }

  def triplesMaps(r2rmlModel: Model): Seq[String] = {

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