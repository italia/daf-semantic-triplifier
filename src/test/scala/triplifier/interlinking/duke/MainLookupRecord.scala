package triplifier.interlinking.duke

import no.priv.garshol.duke.datasources.JDBCDataSource
import no.priv.garshol.duke.datasources.Column
import scala.collection.mutable.LinkedHashMap

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object MainLookupRecord extends App {

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

  src_jdbc_01.addColumn(new Column("_ID", "uriID", "ex", null))
  src_jdbc_01.addColumn(new Column("_COMUNE_NOME", "name", "ex", null))

  src_jdbc_01.getRecords.toStream
    .map { record =>
      val doc = record.getProperties.map { name => (name, record.getValue(name)) }.toStream
      LinkedHashMap(doc: _*)
    }
    .zipWithIndex
    .slice(10, 20)
    .foreach {
      case (doc, i) =>
        println(i, doc)
    }

  //    IDEA: .find(r => r.getOrElse("uriID", "").equalsIgnoreCase("c_d817"))

  val found = DataSourceHelper.lookupByID(src_jdbc_01, "uriID", "c_d817")
  println("FOUND: " + found)

}



