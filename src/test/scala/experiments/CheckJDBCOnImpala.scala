package experiments

import java.sql.DriverManager

object CheckJDBCOnImpala extends App {

  val host = "slave4.platform.daf.gov.it"
  val port = 21050
  val jks_file = "./ssl_impala/master-impala.jks"
  val driver = "com.cloudera.impala.jdbc41.Driver"
  val dsn = s"jdbc:impala://${host}:${port};SSL=1;SSLKeyStore=${jks_file};SSLKeyStorePwd=Ahdai5th;AuthMech=3;CAIssuedCertNamesMismatch=1"
  val username = "aserafini"
  val password = "openD4ti"

  Class.forName(driver)

  val conn = DriverManager.getConnection(dsn, username, password)
  val st = conn.createStatement()

  // CHECK  val tuples = st.executeQuery("""USE opendata; SHOW TABLES""")

  st.executeUpdate("""USE opendata""")

  val tuples = st.executeQuery("""SHOW TABLES""")
  val meta = tuples.getMetaData
  val names = for (i <- 1 to meta.getColumnCount) yield meta.getColumnName(i)
  while (tuples.next()) {
    val cells = names.map { name => (name, tuples.getObject(name)) }.toMap
    println(cells.getOrElse("name", ""))
  }

  st.closeOnCompletion()
  conn.close()
}