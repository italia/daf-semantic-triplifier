package POC.comuni

import java.nio.file.Paths
import java.sql.DriverManager
import java.util.Properties
import java.nio.file.Files
import java.nio.charset.Charset
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import java.sql.Statement
import java.sql.Connection
import scala.util.Try
import scala.collection.mutable.ListBuffer
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import java.io.FileOutputStream

object POCComuni extends App {

  val db_path = Paths.get("C:/Users/Al.Serafini/repos/DAF/db/test_comuni.db").normalize()

  val jdbc_driver = "org.sqlite.JDBC"
  //  val jdbc_dsn = s"jdbc:sqlite:${db_path.toString()}"
  // CHECK
  val jdbc_dsn = "jdbc:sqlite::memory:"

  val jdbc_name = "daf.sqlite"
  val jdbc_user = "aserafini"
  val jdbc_password = "openD4ti"

  Class.forName(jdbc_driver)
  //  DriverManager.registerDriver(driver)

  val prps = new Properties
  val conn = DriverManager.getConnection(jdbc_dsn, prps)

  // create tables
  val ddl_query = SQL.fromFile("src/test/resources/anpr/daf_anpr_ddl.sql")
  JDBC(conn).executeUpdate(ddl_query)

  //  insert data from anpr
  val query_anpr_comuni_insert = SQL.fromFile("src/test/resources/anpr/default_org_o_anpr_archivio_storico_comuni_201803281303.sql")
  JDBC(conn).executeUpdate(query_anpr_comuni_insert)

  //  insert data from istat
  val query_istat_comuni_insert = SQL.fromFile("src/test/resources/anpr/default_org_o_istat_elenco_comuni_italiani_201803281303.sql")
  JDBC(conn).executeUpdate(query_istat_comuni_insert)

  //  extra: insert data from agenzia entrate
  val query_agenziaentrate_comuni_insert = SQL.fromFile("src/test/resources/anpr/default_org_o_agenziaentrate_elenco_comuni_201803281303.sql")
  JDBC(conn).executeUpdate(query_agenziaentrate_comuni_insert)
  //  extra: insert data from anpr
  val query_anpr_stati_esteri_insert = SQL.fromFile("src/test/resources/anpr/default_org_o_anpr_elenco_stati_esteri_201803281303.sql")
  JDBC(conn).executeUpdate(query_anpr_stati_esteri_insert)

  // TESTING RESULTS............................................................
  val out_path = Paths.get("target/export/results.csv").normalize()
  val out_file = out_path.toFile().getAbsoluteFile
  if (!out_file.getParentFile.exists()) out_file.getParentFile.mkdirs()
  if (out_file.exists) Files.delete(out_path)

  val fos = new FileOutputStream(out_file, false)

  JDBC(conn).executeQuery("""
  
    SELECT * 
    FROM `gove__amministrazione.default_org_o_anpr_archivio_storico_comuni` AS ANPR
    LEFT JOIN `gove__amministrazione.default_org_o_istat_elenco_comuni_italiani` AS ISTAT
    ON (ANPR.codistat=ISTAT.codice_comune_formato_alfanumerico) 
    --LIMIT 200
    ;   
    
  """)
    .get
    .zipWithIndex
    .foreach {
      case (row, i) =>
        println(i)
        val line = row.mkString(";")
        fos.write(line.getBytes)
        fos.write("\n".getBytes)
    }

  fos.flush()
  fos.close()

  conn.close()

  // removes the db
  // Files.deleteIfExists(db_path)

}

object SQL {

  def fromFile(path: String): String =
    Files.readAllLines(Paths.get(path), Charset.forName("UTF-8")).mkString("\n")

}

object JDBC {

  def apply(conn: Connection) = new JDBC(conn)

}

class JDBC(conn: Connection) {

  import java.util.{ Map => JMap }

  def executeQuery(query: String): Try[Seq[JMap[String, Object]]] = Try {

    val st = conn.createStatement()

    val rs = st.executeQuery(query)

    val meta = rs.getMetaData
    val cols_names: List[String] = (for (i <- 1 to meta.getColumnCount) yield meta.getColumnName(i)).toList

    val rows = new ListBuffer[JMap[String, Object]]

    while (rs.next()) {

      val row = cols_names.map { col_name =>
        (col_name, rs.getObject(col_name))
      }.toMap

      rows += row

    }

    st.closeOnCompletion()

    rows.toStream

  }

  def executeUpdate(query: String): Try[Int] = Try {

    conn.setAutoCommit(false)
    val st = conn.createStatement()
    val _result = st.executeUpdate(query)
    conn.commit()
    conn.setAutoCommit(true)
    st.closeOnCompletion()
    _result

  }

}
