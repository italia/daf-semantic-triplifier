package repository.sql

import it.almawave.kb.http.utils.JSON
import scala.collection.mutable.LinkedHashMap
import java.security.MessageDigest

object MainETLExampleIPA extends App {

  val repo = new SQLRepository

  val results = repo.query("""
    SELECT *  
    FROM gove__amministrazione.default_org_o_istat_elenco_comuni_italiani 
  """)

  results
    .zipWithIndex
    //    .slice(100, 111)
    .map {
      case (row, i) =>

        val extra_cell1: String = TXT.flatten(row.getOrElse("denominazione_regione", "").asInstanceOf[String])

        val extra_cell2: String = TXT.flatten(row.getOrElse("denominazione_corrente", "").asInstanceOf[String])

        val extra = extra_cell1 + "-" + extra_cell2

        val row_updated = LinkedHashMap(row.toStream: _*) + ("_ROW_NUM" -> i) + ("_EXTRA" -> TXT.hash(extra))

        row_updated
    }
    .foreach { row =>
      println(row)
      println(JSON.writeToString(row))
    }

}

object TXT {

  def hash(txt: String)(implicit algorithm: String = "MD5") = {
    val digest = MessageDigest.getInstance(algorithm)
    digest.digest(txt.getBytes)
  }

  def flatten(txt: String) = {
    txt.replaceAll("[-./,'\";]", " ").trim()
      .replaceAll("\\s+", "-").trim()
      .toLowerCase()
      .replace("à", "a")
      .replace("é", "e")
      .replace("è", "e")
      .replace("è", "e")
      .replace("ì", "i")
      .replace("ò", "o")

  }

}
