package triplifier.interlinking.duke

import no.priv.garshol.duke.datasources.JDBCDataSource
import no.priv.garshol.duke.RecordIterator
import no.priv.garshol.duke.Record
import no.priv.garshol.duke.ConfigLoader
import no.priv.garshol.duke.Processor
import no.priv.garshol.duke.matchers.PrintMatchListener
import no.priv.garshol.duke.ConfigurationImpl
import no.priv.garshol.duke.Configuration
import no.priv.garshol.duke.DataSource
import no.priv.garshol.duke.Logger

import java.util.Collection
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import java.util.ArrayList
import no.priv.garshol.duke.InMemoryLinkDatabase
import no.priv.garshol.duke.InMemoryClassDatabase
import no.priv.garshol.duke.Database
import no.priv.garshol.duke.databases.InMemoryDatabase
import no.priv.garshol.duke.Property
import no.priv.garshol.duke.PropertyImpl
import no.priv.garshol.duke.comparators.Levenshtein
import no.priv.garshol.duke.comparators.JaroWinklerTokenized
import no.priv.garshol.duke.ConfigWriter
import no.priv.garshol.duke.matchers.AbstractMatchListener
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths
import it.almawave.kb.http.utils.JSON
import no.priv.garshol.duke.datasources.Column
import no.priv.garshol.duke.Cleaner
import no.priv.garshol.duke.Duke.NTriplesLinkFileListener
import no.priv.garshol.duke.matchers.LinkDatabaseMatchListener
import scala.collection.mutable.LinkedHashMap
import no.priv.garshol.duke.JDBCLinkDatabase
import java.util.Properties
import repository.sql.SQLRepository
import java.sql.DriverManager
import java.sql.Connection
import java.io.FileInputStream
import scala.xml.Node
import no.priv.garshol.duke.LinkDatabase
import no.priv.garshol.duke.utils.LinkDatabaseUtils
import no.priv.garshol.duke.Link
import org.openrdf.repository.Repository
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.repository.RepositoryConnection
import org.openrdf.model.ValueFactory
import org.openrdf.model.Resource

/**
 * SEE: https://medium.com/@simrnsethi/how-to-use-duke-for-deduplication-of-bigdata-aaae85221707
 */
object MainDuke extends App {

  val src_jdbc_01 = new JDBCDataSource
  src_jdbc_01.setConnectionString("jdbc:impala://slave4.platform.daf.gov.it:21050;SSL=1;SSLKeyStore=./ssl_impala/master-impala.jks;SSLKeyStorePwd=Ahdai5th;AuthMech=3;CAIssuedCertNamesMismatch=1")
  src_jdbc_01.setDriverClass("com.cloudera.impala.jdbc41.Driver")
  src_jdbc_01.setUserName("aserafini")
  src_jdbc_01.setPassword("openD4ti")
  src_jdbc_01.setQuery("""
    SELECT DISTINCT 
    	AMM.regione AS _REGIONE_NOME
    	,AMM.comune AS _COMUNE_NOME 
    	,AMM.cod_amm AS _ID
    FROM opendata.agid_o_amministrazioni_stage_parquet AS AMM
  """)

  src_jdbc_01.addColumn(new Column("_ID", "_ID", "ex", null))
  src_jdbc_01.addColumn(new Column("_COMUNE_NOME", "_NAME", "ex", null))

  val src_jdbc_02 = new JDBCDataSource
  src_jdbc_02.setConnectionString("jdbc:impala://slave4.platform.daf.gov.it:21050;SSL=1;SSLKeyStore=./ssl_impala/master-impala.jks;SSLKeyStorePwd=Ahdai5th;AuthMech=3;CAIssuedCertNamesMismatch=1")
  src_jdbc_02.setDriverClass("com.cloudera.impala.jdbc41.Driver")
  src_jdbc_02.setUserName("aserafini")
  src_jdbc_02.setPassword("openD4ti")
  src_jdbc_02.setQuery("""
    SELECT DISTINCT 
    	ISTAT.denominazione_regione AS _REGIONE_NOME
    	,ISTAT.denominazione_corrente AS _COMUNE_NOME 
    	,ISTAT.codice_comune_formato_alfanumerico AS _ID 
    FROM gove__amministrazione.default_org_o_istat_elenco_comuni_italiani AS ISTAT
    ORDER BY _ID 
  """)

  src_jdbc_02.addColumn(new Column("_ID", "_ID", "ex", null))
  src_jdbc_02.addColumn(new Column("_COMUNE_NOME", "_NAME", "ex", null))

  val pid = new PropertyImpl("_ID")
  val prop = new PropertyImpl("_NAME", new Levenshtein, 0.1, 0.95)

  val props = new ArrayList[Property]
  props.add(pid)
  props.add(prop)

  val config = new ConfigurationImpl

  // WARNING: avoid usage of 0!!
  config.addDataSource(1, src_jdbc_01)
  config.addDataSource(2, src_jdbc_02)

  val db = new InMemoryDatabase
  config.setDatabase(db)
  config.setProperties(props)
  config.setThreshold(0.1)
  config.setMaybeThreshold(0.1)

  config.validate()

  //  System.out.println("IDENTITY? ",config.getIdentityProperties())
  //  System.exit(0)

  // test writer
  val config_path = Paths.get("target/duke/duke_example.xml").toAbsolutePath().normalize()
  val config_file = Files.createDirectories(config_path.getParent)
  ConfigWriter.write(config, config_path.toString())

  val processor = new Processor(config)

  val collectorListener = new CollectorListener
  processor.addMatchListener(collectorListener)

  val db_links2 = new JDBCLinkDatabase(
    "org.h2.Driver",
    "jdbc:h2:file:./target/duke/test_links.db",
    "h2", new Properties())

  //  val db_links = new InMemoryLinkDatabase
  // CHECK: LinkDatabaseUtils.loadTestFile(...)

  val db_links = new RDFRepositoryLinkDatabase("http://interlinking/")

  val linkListener = new LinkDatabaseMatchListener(config, db_links)
  processor.addMatchListener(linkListener)

  processor.link(
    config.getDataSources(1),
    config.getDataSources(2),
    false,
    1000)

  db_links.getAllLinks
    .slice(10, 22)
    .zipWithIndex
    .foreach {
      case (link, i) =>
        println(i + ": " + link)
    }

  processor.close()

}

class RDFRepositoryLinkDatabase(context: String) extends LinkDatabase {

  val repo: Repository = new SailRepository(new MemoryStore)

  var conn: RepositoryConnection = null
  var vf: ValueFactory = null
  var ctx: Resource = null

  def validateConnection(): Unit = ???

  def assertLink(link: Link): Unit = ???

  def getAllLinks(): Collection[Link] = ???
  def getAllLinksFor(x$1: String): Collection[Link] = ???
  def getChangesSince(x$1: Long): Collection[Link] = ???
  def inferLink(x$1: String, x$2: String): Link = ???

  def clear() {
    conn.clear(ctx)
  }

  def commit(): Unit = ???

  def start() {
    if (!repo.isInitialized()) repo.initialize()
    if (conn == null) conn = repo.getConnection
    if (vf == null) vf = conn.getValueFactory
    if (ctx == null) vf.createURI(context)
  }
  def stop() {
    conn.close()
    conn = null
  }

  def close() {

  }

}