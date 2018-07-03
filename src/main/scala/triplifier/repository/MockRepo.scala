package triplifier.repository

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import java.net.URL
import org.openrdf.rio.Rio
import org.openrdf.rio.RDFFormat
import java.io.File
import org.openrdf.query.QueryLanguage
import org.openrdf.query.Query
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter
import org.openrdf.query.resultio.TupleQueryResultWriter
import org.openrdf.query.resultio.QueryResultIO
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter
import org.openrdf.query.parser.sparql.SPARQLParserFactory
import scala.util.Try
import org.openrdf.query.parser.ParsedOperation
import info.aduna.io.IOUtil
import org.openrdf.query.parser.ParsedTupleQuery
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase
import org.openrdf.query.algebra.QueryModelVisitor
import org.openrdf.query.TupleQuery
import org.openrdf.query.GraphQuery
import org.openrdf.query.parser.ParsedQuery
import org.openrdf.query.parser.QueryParserUtil
import org.openrdf.query.parser.ParsedBooleanQuery
import org.openrdf.query.parser.ParsedGraphQuery
import java.io.OutputStream
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter

/**
 * this example should be extended to expose a minimal, simple SPARQL endpoint
 */
object MainMockRepo extends App {

  val rdf_url = new File("target/DUMP/test/territorial-classifications/regions.ttl")
    .getAbsoluteFile
    .toURI().toURL()

  val mock = new MockRepo()
  mock.load(rdf_url)

  val _query = """
  
    SELECT ?concept (COUNT(?s) AS ?triples)
    #CONSTRUCT { ?s ?p ?o } 
    WHERE {
      ?s a ?concept .
      ?s ?p ?o .
    }  
    GROUP BY ?concept 
    
  """

  //  mock.check_query(_query)

  mock.query(_query, "json")(System.out)

}

class MockRepo() {

  val baseURI = "http://sparql/"

  val repo = new SailRepository(new MemoryStore)
  if (!repo.isInitialized()) repo.initialize()

  def query(query: String, format: String = "csv")(out: OutputStream) = {

    val parsed = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, baseURI)

    val conn = repo.getConnection

    parsed match {

      case tuples: ParsedTupleQuery =>
        val result_format = QueryResultIO.getWriterFormatForFileName("DUMP." + format)
        val writer = QueryResultIO.createWriter(result_format, out)
        conn.prepareTupleQuery(QueryLanguage.SPARQL, query)
          .evaluate(writer)

      case bool: ParsedBooleanQuery =>
        val result = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query).evaluate()
        out.write(result.toString().getBytes)

      case graph: ParsedGraphQuery =>
        val writer = Rio.createWriter(Rio.getWriterFormatForFileName("DUMP." + format), out)
        conn.prepareGraphQuery(QueryLanguage.SPARQL, query, baseURI)
          .evaluate(writer)

      case _ => throw new RuntimeException("CAN'tPARSE!...")

    }

    conn.close()
  }

  def load(rdf_url: URL) {
    val rdf_format = Rio.getParserFormatForFileName(rdf_url.getPath.toString(), RDFFormat.TURTLE)
    val conn = repo.getConnection
    conn.begin()
    conn.add(rdf_url, "", rdf_format)
    conn.commit()
    conn.close()
  }

}


//        val writer = new SPARQLResultsCSVWriter(out)
//        val writer = new SPARQLResultsJSONWriter(out)
