package repository.triplestore

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
import java.nio.file.Paths
import org.openrdf.query.resultio.TupleQueryResultFormat
import org.openrdf.query.resultio.BooleanQueryResultFormat
import org.openrdf.repository.Repository
import org.openrdf.repository.RepositoryConnection
import org.slf4j.LoggerFactory

/**
 * this example should be extended to expose a minimal, simple SPARQL endpoint
 */
object MainMockRepo extends App {

  val rdf_url = Paths.get("target/DUMP/test/territorial-classifications/regions.ttl")
    .toAbsolutePath().normalize()
    .toUri().toURL()

  println("rdf_url: " + rdf_url)

  val mock = new MockRepo()
  mock.start()
  //  mock.load(rdf_url)

  mock.endpoint.update(s"""
    LOAD <${rdf_url.toURI()}>
    INTO GRAPH <test://regions.ttl>
  """)

  val _query2 = """
    CONSTRUCT { ?s ?p ?o } 
    WHERE {
      ?s a ?concept .
      ?s ?p ?o .
    }  
  """

  val _query = """
    SELECT ?concept (COUNT(?s) AS ?triples) ?graph 
    WHERE {
      GRAPH ?graph {
        ?s a ?concept .
        ?s ?p ?o .
      }
    }  
    GROUP BY ?graph ?concept 
  """

  //  mock.check_query(_query)

  mock.endpoint.query(_query, "csv")(System.out)

  mock.stop()

}

class MockRepo(baseURI: String = "https://baseURI/") {

  val logger = LoggerFactory.getLogger(this.getClass)
  val repo = new SailRepository(new MemoryStore)

  def start() = {
    logger.info("#### RDF repository START")
    if (!repo.isInitialized()) repo.initialize()
  }

  def stop() = {
    logger.info("#### RDF repository STOP")
    if (repo.isInitialized()) repo.shutDown()
  }

  def repositoryAction[RES](query: String)(action: (RepositoryConnection) => RES): RES = {
    val _conn = repo.getConnection
    val result = action(_conn)
    _conn.close()
    result
  }

  object endpoint {

    def update(query: String) {

      val conn = repo.getConnection
      conn.begin()
      conn.prepareUpdate(QueryLanguage.SPARQL, query, baseURI).execute()
      conn.commit()
      conn.close()

    }

    def query(query: String, format: String = "csv")(out: OutputStream) = {

      val parsed = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, baseURI)

      val conn = repo.getConnection

      parsed match {

        case tuples: ParsedTupleQuery =>

          val result_format: TupleQueryResultFormat = QueryResultIO.getWriterFormatForFileName("DUMP." + format)
          val writer = QueryResultIO.createWriter(result_format, out)
          conn.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate(writer)

        case bool: ParsedBooleanQuery =>

          val result_format: BooleanQueryResultFormat = QueryResultIO.getBooleanWriterFormatForFileName("DUMP." + format)
          val writer = QueryResultIO.createWriter(result_format, out)
          val result = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query).evaluate()
          out.write(result.toString().getBytes)

        case graph: ParsedGraphQuery =>

          val result_format: RDFFormat = Rio.getWriterFormatForFileName("DUMP." + format, RDFFormat.NTRIPLES)

          val writer = Rio.createWriter(result_format, out)
          conn.prepareGraphQuery(QueryLanguage.SPARQL, query, baseURI).evaluate(writer)

        case _ => throw new RuntimeException(s"can't parse query\n${query}\nwith format ${format}")

      }

      conn.close()
    }

  }

  def load(rdf_url: URL) = {
    logger.debug(s"REPO> loading data from ${rdf_url}")
    val rdf_format = Rio.getParserFormatForFileName(rdf_url.getPath.toString(), RDFFormat.TURTLE)
    val conn = repo.getConnection
    conn.begin()
    conn.add(rdf_url, baseURI, rdf_format, null)
    conn.commit()
    conn.close()
  }

}


