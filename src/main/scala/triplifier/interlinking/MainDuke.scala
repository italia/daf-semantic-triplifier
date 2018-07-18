package triplifier.interlinking

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

object MainDuke extends App {

  val src_jdbc_01 = new JDBCDataSource
  src_jdbc_01.setConnectionString("jdbc:impala://slave4.platform.daf.gov.it:21050;SSL=1;SSLKeyStore=./ssl_impala/master-impala.jks;SSLKeyStorePwd=Ahdai5th;AuthMech=3;CAIssuedCertNamesMismatch=1")
  src_jdbc_01.setDriverClass("com.cloudera.impala.jdbc41.Driver")
  src_jdbc_01.setUserName("aserafini")
  src_jdbc_01.setPassword("openD4ti")
  src_jdbc_01.setQuery("""
    SELECT DISTINCT 
      AMM.comune AS _NAME 
      ,AMM.cod_amm AS _ID 
    FROM opendata.agid_o_amministrazioni_stage_parquet AS AMM
  """)

  val src_jdbc_02 = new JDBCDataSource
  src_jdbc_02.setConnectionString("jdbc:impala://slave4.platform.daf.gov.it:21050;SSL=1;SSLKeyStore=./ssl_impala/master-impala.jks;SSLKeyStorePwd=Ahdai5th;AuthMech=3;CAIssuedCertNamesMismatch=1")
  src_jdbc_02.setDriverClass("com.cloudera.impala.jdbc41.Driver")
  src_jdbc_02.setUserName("aserafini")
  src_jdbc_02.setPassword("openD4ti")
  src_jdbc_02.setQuery("""
    SELECT DISTINCT 
      ISTAT.denominazione_corrente AS _NAME, 
      ISTAT.codice_comune_formato_alfanumerico AS _ID 
    FROM gove__amministrazione.default_org_o_istat_elenco_comuni_italiani AS ISTAT
    ORDER BY _ID 
  """)

  // ------------

  val db = new InMemoryDatabase

  val pid = new PropertyImpl("_ID")
  val prop = new PropertyImpl("_NAME", new Levenshtein, 0.3, 0.95)

  val props = new ArrayList[Property]
  props.add(pid)
  props.add(prop)

  val config = new ConfigurationImpl

  // WARNING: avoid usage of 0!!
  config.addDataSource(1, src_jdbc_01)
  config.addDataSource(2, src_jdbc_02)

  config.setDatabase(db)
  config.setProperties(props)
  config.setThreshold(0.1)
  config.setMaybeThreshold(0.1)

  config.validate()

  // test writer
  ConfigWriter.write(config, "target/duke_example.xml")

  // test writer

  val processor = new Processor(config, true)

  //  val match_listener = new PrintMatchListener(true, true, true, true, props, true)
  //  processor.addMatchListener(match_listener)

  val match_listener = new AbstractMatchListener {

    val logger = LoggerFactory.getLogger(this.getClass)

    var count = 0

    def getMatchCount = count

    override def matches(r1: Record, r2: Record, confidence: Double) {
      count += 1
      println(r1, r2, confidence, count)
      val test = r1.getValue("_NAME").equalsIgnoreCase(r2.getValue("_NAME"))
      println(s"TEST: ${test} = ${r1}:${r2}")
    }

    override def matchesPerhaps(r1: Record, r2: Record, confidence: Double) {
    }

    override def startProcessing() {
      logger.debug("duke> LISTENER.START")
    }

    override def endProcessing() {
      logger.debug("duke> LISTENER.STOP")
    }

  }

  processor.addMatchListener(match_listener)

  processor.link(
    config.getDataSources(1),
    config.getDataSources(2),
    true,
    1000)

  val matches = match_listener.getMatchCount
  println(s"\n\nDUKE> FOUND ${matches} matches")

  processor.close()

  // TESTING SINGLE SOURCE
  //  val it: RecordIterator = src_jdbc_02.getRecords()
  //  val rit = new Iterator[Record] {
  //    def hasNext: Boolean = it.hasNext()
  //    def next(): Record = it.next()
  //  }
  //  var i = 0
  //  rit.foreach { rec =>
  //    println(rec)
  //    i += 1
  //  }
  //  println("TOTAL: " + i)

  //  SEE: https://medium.com/@simrnsethi/how-to-use-duke-for-deduplication-of-bigdata-aaae85221707
}