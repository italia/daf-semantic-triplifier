package repository.sql

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.sql.DriverManager
import scala.collection.mutable.ListBuffer
import java.sql.ResultSet
import scala.collection.mutable.LinkedHashMap

class SQLRepository(conf: Config = ConfigFactory.empty()) {

  Class.forName("com.cloudera.impala.jdbc41.Driver")

  val conn = DriverManager.getConnection(
    "jdbc:impala://slave4.platform.daf.gov.it:21050;SSL=1;SSLKeyStore=C:/Users/Al.Serafini/awavedev/progetti/DAF/ssl_impala/master-impala.jks;SSLKeyStorePwd=Ahdai5th;AuthMech=3;CAIssuedCertNamesMismatch=1",
    "aserafini",
    "openD4ti")

  def query(query: String): Seq[Map[String, Any]] = {

    val st = conn.createStatement()

    val rs = st.executeQuery(query)

    val meta = rs.getMetaData
    val names = for (i <- 1 to meta.getColumnCount) yield meta.getColumnName(i)

    val list = new ListBuffer[Map[String, Any]]

    var i = 0
    while (rs.next()) {

      println("iteration: " + i)
      val map = LinkedHashMap(names.map { n => (n, rs.getObject(n)) }: _*)

      list += map.toMap

      i = i + 1
    }

    st.close()

    conn.close()

    list.toStream

  }

}