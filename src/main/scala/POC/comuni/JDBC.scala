package POC.comuni

import java.sql.Connection
import scala.collection.mutable.ListBuffer
import scala.util.Try
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import scala.util.Properties
import java.util.Properties
import java.sql.DriverManager
import org.sqlite.Function

object JDBC {

  def apply(conf: JDBCConfig = JDBCConfig.DEFAULT) = new JDBC(conf)

}

class JDBC(conf: JDBCConfig) {

  import java.util.{ Map => JMap }

  var conns: List[Connection] = List()

  def start() {

    Class.forName(conf.driver)
    //  DriverManager.registerDriver(driver)

    val prps = new Properties
    val conn = DriverManager.getConnection(conf.dsn, prps)

    // register SQlite UDF
    // Function.create(conn, "normalize", new SQLiteUDFs.normalize())

    // TODO: use a connection pool!
    conns = List(conn)

  }

  def stop() {

    conns.foreach(_.close())

  }

  // TODO: use a connection pool!
  private def _connection(): Connection = conns(0)

  //  def executeQuery(query: String): Try[Seq[JMap[String, Object]]] = Try {
  def executeQuery(query: String): Try[Seq[Seq[(String, Object)]]] = Try {

    val st = _connection.createStatement()

    val rs = st.executeQuery(query)

    val meta = rs.getMetaData
    val cols_names: List[String] = (for (i <- 1 to meta.getColumnCount) yield meta.getColumnName(i)).toList

    val rows = new ListBuffer[List[(String, Object)]]

    while (rs.next()) {

      val row = cols_names.map(col_name => (col_name, rs.getObject(col_name)))

      rows += row

    }

    st.closeOnCompletion()

    rows.toStream

  }

  def executeUpdate(query: String): Try[Int] = Try {

    _connection.setAutoCommit(false)
    val st = conns(0).createStatement()
    val _result = st.executeUpdate(query)
    _connection.commit()
    _connection.setAutoCommit(true)
    st.closeOnCompletion()
    _result

  }

}

case class JDBCConfig(
  driver:     String,
  dsn:        String,
  name:       String,
  user:       String,
  password:   String,
  properties: Properties = new Properties()) {

  def with_dsn(dsn_new: String) =
    this.copy(driver, dsn_new, name, user, password, properties)

}

object JDBCConfig {

  def DEFAULT = JDBCConfig(
    "org.sqlite.JDBC",
    "jdbc:sqlite::memory:",
    "in_memory",
    "",
    "")

  // EX: JDBCConfig.parse(JDBCConfig.IN_MEMORY_CONFIG)
  def parse(conf: Config) = JDBCConfig(
    conf.getString("driver"),
    conf.getString("dsn"),
    conf.getString("name"),
    conf.getString("user"),
    conf.getString("password"))

}

object SQLiteUDFs {

  class normalize extends Function {

    override def xFunc() {

      val txt = value_text(0).head.toUpper + value_text(0).tail.toLowerCase()
      println(txt)
      result(txt)

    }
  }

}
  