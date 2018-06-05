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
import java.util.{ Map => JMap }
import java.nio.file.attribute.FileAttribute

object DBComuniPOC extends App {

  val db_path = Paths.get("./db/test_anpr_comuni.db").toAbsolutePath().normalize()
  if (!db_path.getParent.toFile().exists()) db_path.getParent.toFile().mkdirs()

  val jdbc_dsn = s"jdbc:sqlite:${db_path.toString()}"

  val jdbc = JDBC(JDBCConfig.DEFAULT.with_dsn(jdbc_dsn))
  jdbc.start()

  // create tables
  val ddl_query = SQL.fromFile("src/test/resources/anpr/daf_anpr_ddl.sql")
  jdbc.executeUpdate(ddl_query)

  //  insert data from anpr
  val query_anpr_comuni_insert = SQL.fromFile("src/test/resources/anpr/default_org_o_anpr_archivio_storico_comuni_201803281303.sql")
  jdbc.executeUpdate(query_anpr_comuni_insert)

  //  insert data from istat
  val query_istat_comuni_insert = SQL.fromFile("src/test/resources/anpr/default_org_o_istat_elenco_comuni_italiani_201803281303.sql")
  jdbc.executeUpdate(query_istat_comuni_insert)

  //  extra: insert data from agenzia entrate
  val query_agenziaentrate_comuni_insert = SQL.fromFile("src/test/resources/anpr/default_org_o_agenziaentrate_elenco_comuni_201803281303.sql")
  jdbc.executeUpdate(query_agenziaentrate_comuni_insert)
  //  extra: insert data from anpr
  val query_anpr_stati_esteri_insert = SQL.fromFile("src/test/resources/anpr/default_org_o_anpr_elenco_stati_esteri_201803281303.sql")
  jdbc.executeUpdate(query_anpr_stati_esteri_insert)

  // TESTING RESULTS............................................................
  val out_path = Paths.get("target/export/test_anpr_comuni.csv").normalize()
  val out_file = out_path.toFile().getAbsoluteFile
  if (!out_file.getParentFile.exists()) out_file.getParentFile.mkdirs()
  if (out_file.exists) Files.delete(out_path)

  val csv = CSV(";", "\"")

  val fos = new FileOutputStream(out_file, false)

  val results = jdbc.executeQuery("""
  
      SELECT *
      FROM `gove__amministrazione.default_org_o_anpr_archivio_storico_comuni` AS ANPR
      LEFT JOIN `gove__amministrazione.default_org_o_istat_elenco_comuni_italiani` AS ISTAT
      ON (ANPR.codistat=ISTAT.codice_comune_formato_alfanumerico)
      --LIMIT 200
      ;
  
    """)
    .get

  csv.writeAsString(results)
    .zipWithIndex
    .foreach {
      case (line, i) =>
        println(i)
        fos.write(line.getBytes)
        fos.write("\n".getBytes)
    }

  fos.flush()
  fos.close()

  jdbc.stop()

  // removes the db
  Files.deleteIfExists(db_path)

}




