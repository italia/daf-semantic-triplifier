package triplifier.processors

import com.typesafe.config.Config
import java.sql.DriverManager
import it.almawave.kb.http.utils.JSON
import scala.collection.mutable.ListBuffer
import scala.math.Ordering
import scala.collection.mutable.LinkedHashMap
import it.almawave.linkeddata.kb.utils.ModelAdapter
import java.sql.Connection
import org.slf4j.LoggerFactory
import it.almawave.linkeddata.kb.utils.ModelAdapter
import java.io.OutputStream
import java.security.MessageDigest
import org.openrdf.model.Model
import java.io.ByteArrayOutputStream
import org.openrdf.rio.Rio
import org.openrdf.rio.RDFFormat
import java.io.ByteArrayInputStream
import com.typesafe.config.ConfigFactory

object MainMapperExtraction extends App {

  val conf = OntopProcessor.sqlite.conf
  //  val conf = OntopProcessor.impala.conf

  println(conf)

  val test = new RDFMapper(conf)
  test.start()

  val query = """
    SELECT *
    FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani'
  """

  //  val query = """
  //
  //      SELECT
  //        #codice_catastale_del_comune,
  //        progressivo_del_comune,
  //        codice_comune_formato_alfanumerico,
  //        denominazione_corrente,
  //        codice_provincia,
  //        denominazione_provincia,
  //        codice_regione,
  //        denominazione_regione
  //      FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani'
  //
  //    """

  //  CHECK LIMIT 100

  val db_meta = test.extract_metadata_db()
  println(s"METADATA")
  println(JSON.writeToString(db_meta))

  val cells_meta = test.extract_metadata_query(query)
  cells_meta.foreach(m => println(JSON.writeToString(m)))

  val del = db_meta.getOrElse("IdentifierQuoteString", "")

  val table_name = test.quoted_table_name(test.extract_table_name(query))
  //s"""${del}${test.extract_table_name(query)}${del}"""
  println(s"TABLE NAME: ${table_name}")
  val cols_names = test.extract_column_names(query)
  println(s"COLUMNS: ${cols_names.mkString("|")}")

  val col_test = test.analyze_columns_distinct(s"${table_name}", cols_names)
  println(JSON.writeToString(col_test))

  val keys_candidates = test.analyze_keys_candidates(table_name, cols_names)
  println(s"KEYS CANDIDATES:")
  println(JSON.writeToString(keys_candidates))

  val entities_candidates = test.analyze_entities_keys_candidates(table_name, cols_names)
  println(s"ENTITIES CANDIDATES:")
  println(JSON.writeToString(entities_candidates))

  val analysis = test.analyze(query)
  println(s"ANALYSIS:")
  println(JSON.writeToString(analysis))

  println("\n\n\n\nR2RML mapping.............................................")
  val r2rml = test.generate_mapping(query, "https://w3id.org/italia/data")

  println(r2rml)

  test.stop()

}
