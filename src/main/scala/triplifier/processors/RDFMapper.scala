package triplifier.processors

import java.io.ByteArrayOutputStream
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.LinkedHashMap
import it.almawave.linkeddata.kb.utils.ModelAdapter

object RDFMapper {

  def apply(config: String) = new RDFMapper(ConfigFactory.parseString(config).resolve())

}

class RDFMapper(conf: Config) {

  val logger = LoggerFactory.getLogger(this.getClass)

  Class.forName(conf.getString("jdbc.driver"))

  var conn: Connection = null

  def start() {
    conn = DriverManager.getConnection(
      conf.getString("jdbc.dsn"),
      conf.getString("jdbc.user"),
      conf.getString("jdbc.password"))
  }

  def stop() {
    conn.close()
  }

  lazy private val db_meta = this.extract_metadata_db()
  //  lazy private val del = db_meta.getOrElse("IdentifierQuoteString", "")

  def quoted_table_name(name: String) = {

    val db_product = db_meta.get("DatabaseProductName").orElse(Some("Impala")).get
    val D = db_meta.get("IdentifierQuoteString").orElse(Some("")).get

    // VERIFY: CatalogSeparator

    if (db_product.equals("Impala"))
      name.split(".").map(el => s"""${D}${el}${D}""").mkString("")
    else
      s"""${D}${name}${D}"""

  }

  def execute(query: String): Seq[Map[String, Object]] = {

    val st = conn.createStatement()
    val rs = st.executeQuery(query)

    val names = { for (i <- 1 to rs.getMetaData.getColumnCount) yield rs.getMetaData.getColumnName(i) }.toList

    val rows = new ListBuffer[Map[String, Object]]

    while (rs.next()) {
      val els = names.map { name => (name, rs.getObject(name)) }
      val row = LinkedHashMap(els: _*).toMap // ChECK order...
      rows += row
    }

    rs.close()
    st.closeOnCompletion()

    rows.toStream

  }

  // TODO: case class
  def extract_metadata_db() = {

    val meta = conn.getMetaData

    Map(
      "IdentifierQuoteString" -> meta.getIdentifierQuoteString,
      "SearchStringEscape" -> meta.getSearchStringEscape,
      "CatalogSeparator" -> meta.getCatalogSeparator,
      "DatabaseProductName" -> meta.getDatabaseProductName,
      "DatabaseMajorVersion" -> meta.getDatabaseMajorVersion,
      "DatabaseMinorVersion" -> meta.getDatabaseMinorVersion,
      "DatabaseProductVersion" -> meta.getDatabaseProductVersion)

  }

  def guess_table_name(query: String): String = {

    val guessed = query
      .replaceAll("\\s+", " ")
      .replaceAll(".*?FROM\\s+(.*?)\\s+.*", "$1")
      .replaceAll("\"", "").replaceAll("'", "").replaceAll("`", "")

    println(s"""
      GUESS TABLE NAME: <${guessed}>
    """)

    guessed
  }

  def extract_metadata_query(query: String): Seq[ColumnMeta] = {

    val table_guessed = guess_table_name(query) // HACK in case jdbc.metadata are lacking (ex: impala)

    val st = conn.createStatement()

    val rs = st.executeQuery(query)

    val meta = rs.getMetaData

    val cells_meta = for { n <- 1 to meta.getColumnCount } yield {

      val name = meta.getColumnName(n)
      val label = meta.getColumnLabel(n)
      val `type` = meta.getColumnType(n)
      val `type_name` = meta.getColumnTypeName(n)
      val catalog = meta.getCatalogName(n)
      val schema = meta.getSchemaName(n) // CHECK on impala
      var table = meta.getTableName(n) // CHECK on impala
      table = if (table != null && !table.trim().equals("")) table else table_guessed

      val auto_increment = meta.isAutoIncrement(n)
      val case_sensitive = meta.isCaseSensitive(n)
      val currency = meta.isCurrency(n)
      val nullable = (meta.isNullable(n) != 0 && meta.isNullable(n) == 1 && meta.isNullable(n) == 2)
      val searchable = meta.isSearchable(n)
      val signed = meta.isSigned(n)

      ColumnMeta(n, name, label, `type`, `type_name`, catalog, schema, table)

    }

    rs.close()
    st.closeOnCompletion()

    cells_meta
  }

  def extract_column_names(query: String) = extract_metadata_query(query).map(_.name).toList

  def extract_table_name(query: String) = extract_metadata_query(query).head.table

  def extract_num_rows(table_name: String): Long = {
    val query = s"""SELECT COUNT(*) AS size FROM ${table_name}"""
    logger.debug(s"""SQL>\n${query}""")
    ModelAdapter.fromMap[COUNT](execute(query).head).size
  }

  def distinct_values_for_column(table_name: String, column: String = ""): Long = {
    val query = s"""SELECT COUNT(DISTINCT ${column}) AS size FROM ${table_name}"""
    logger.debug(s"""SQL>\n${query}""")
    ModelAdapter.fromMap[COUNT](execute(query).head).size
  }

  def analyze_columns_distinct(table_name: String, names: Seq[String]): LinkedHashMap[String, Long] = {

    val results = names
      .par
      .toList
      .map { name => (name, distinct_values_for_column(table_name, s"${name}")) }
      .sortWith { case ((_, a), (_, b)) => a >= b }

    collection.mutable.LinkedHashMap(results: _*)

  }

  def analyze_keys_candidates(table_name: String, names: Seq[String]): LinkedHashMap[Long, List[String]] = {

    val results = analyze_columns_distinct(table_name, names)
      .groupBy(_._2)
      .map { e => (e._1, e._2.map(_._1).toList) }
      .toList
      .sortWith { case ((a, _), (b, _)) => a >= b }

    collection.mutable.LinkedHashMap(results: _*)

  }

  def analyze_entities_keys_candidates(table_name: String, names: Seq[String]) = {

    analyze_keys_candidates(table_name, names).filter(_._2.size > 1)

  }

  private def guess_subjects(size: Long, elements: LinkedHashMap[Long, List[String]]) = {

    elements.filterKeys(_ == size).flatMap(_._2)

  }

  private def guess_foreign_subjects(size: Long, elements: LinkedHashMap[Long, List[String]]): Seq[Seq[String]] = {

    elements.filterKeys(_ != size).toList.map(_._2)

  }

  case class Reference(name: String, type_class: String)

  def analyze(query: String) = {

    val cells_meta = this.extract_metadata_query(query)

    val table_name = this.quoted_table_name(this.extract_table_name(query)) //s"""${del}${this.extract_table_name(query)}${del}"""
    val cols_names = this.extract_column_names(query)

    val rows_size = extract_num_rows(table_name)

    val cols_distinct = this.analyze_columns_distinct(s"${table_name}", cols_names)

    val keys_candidates = this.analyze_keys_candidates(table_name, cols_names)

    val entities_candidates = this.analyze_entities_keys_candidates(table_name, cols_names)

    val subjects = guess_subjects(rows_size, entities_candidates)
    val foreign_subjects = guess_foreign_subjects(rows_size, entities_candidates)
    val properties_data = cells_meta.map { c => (c.name, c.type_name) }

    LinkedHashMap(
      "db.meta" -> db_meta,
      "query.columns.meta" -> cells_meta,
      "query.rows.total" -> rows_size,
      "query.columns.names" -> cols_names,
      "query.columns.distinct" -> cols_distinct,
      "query.aggs.valuesByColumns" -> keys_candidates,
      "query.aggs.valuesByColumnsGroup" -> entities_candidates,
      "query.sql" -> query.trim().replaceAll("\\s+", " "),
      "query.guess.subjects" -> subjects,
      "query.guess.datatype_properties" -> properties_data,
      "query.guess.object_properties" -> foreign_subjects)

  }

  def generate_mapping(query: String, base_uri: String): String = {

    val map = analyze(query: String)

    def r2rml_subject(sub_ref: String, base_uri: String): String = {

      val sub_map = s"""
  		rr:subjectMap [
    		rr:template "${base_uri}/{'${sub_ref}'}" ;  
    		rr:class owl:Class, skos:Concept ;
  		] ;
  		"""

      val sub_map_fallback = """
    	# NOTE: the choosen query seems to not contain unique identifiers! 
    	# TODO: use rr:template "${base_uri}/SOME_ID" ; instead of blank nodes here
    	rr:subjectMap [
    	  rr:termType rr:BlankNode ;
    	  rr:class owl:Thing ;
    	] ; 
    	"""

      if (sub_ref != null) sub_map else sub_map_fallback

    }

    def r2rml_datatype_properties(prp_name: String, base_uri: String): String = {
      val prp_id = prp_name.trim().toLowerCase().replace("\\s+", "-")
      s"""
        rr:predicateObjectMap [
          rr:predicate ex:${prp_id} ;
          rr:objectMap [ rr:column "${prp_name}" ; rr:datatype xsd:string ]
        ] ;
      """
    }

    def r2rml_query(query: String): String = {

      val query_id = query.getBytes.map(_.toLong).sum

      s"""
        <VIEW_${query_id}> rr:sqlQuery \"\"\"
          ${query}
        \"\"\"
        .
      """

    }

    def r2rml_resource(sub_ref: String, prps: Seq[String], base_uri: String): String = {

      val query_id = query.getBytes.map(_.toLong).sum

      val buffer = new StringBuffer

      buffer.append(r2rml_subject(sub_ref, base_uri))
      prps.foreach { prp =>
        buffer.append(r2rml_datatype_properties(prp, base_uri))
        //        buffer.append("\n")
      }

      s"""
      
      #### draft mapping for ${sub_ref}

      <TriplesMap_${sub_ref}> a rr:TriplesMapClass ;
        
        rr:logicalTable <VIEW_${query_id}> ;
        
          ${buffer.toString()}
        
      .
        
      """

    }

    def r2rml_prefixes(query: String, base_uri: String) = {
      s"""
      @prefix owl: <http://www.w3.org/2002/07/owl#> .
      @prefix rr: <http://www.w3.org/ns/r2rml#> .
      @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
      @prefix ex: <http://example.org/> .
      
      @base  <${base_uri}> .  
      """
    }

    val subject = map.get("query.guess.subjects").get
      .asInstanceOf[List[String]]
      .headOption.getOrElse(null) // REVIEW: usage of null here!

    val datatype_properties = map.get("query.guess.datatype_properties").get
      .asInstanceOf[Seq[Tuple2[String, String]]]
      .map(_._1)

    val external_entities = map.get("query.guess.object_properties").get
      .asInstanceOf[List[List[String]]]

    // R2RML draft serialization
    val out = new ByteArrayOutputStream
    out.write(r2rml_prefixes(query, base_uri).getBytes)
    out.write(r2rml_query(query).getBytes)
    out.write(r2rml_resource(subject, datatype_properties, base_uri).getBytes)
    external_entities.foreach { entities =>
      out.write(r2rml_resource(entities.head, entities, base_uri).getBytes)
    }
    out.write("\n\n".getBytes)

    out.toString()
  }

}

case class COUNT(size: Long)

case class ColumnMeta(
  `order`:     Int,
  `name`:      String,
  `label`:     String,
  `type`:      Int,
  `type_name`: String,
  `catalog`:   String,
  `schema`:    String,
  `table`:     String)