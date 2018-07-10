package triplifier.endpoints.testing

import io.swagger.annotations.Api
import javax.ws.rs.Path
import javax.ws.rs.core.Response
import javax.ws.rs.POST
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.query.QueryLanguage
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter
import javax.ws.rs.core.StreamingOutput
import java.io.OutputStream

@Api(tags = Array("SPARQL"))
@Path("/sparql")
class MockSPARQLEndpoint {

  val repo = new SailRepository(new MemoryStore)
  repo.initialize()

  @POST
  def sparql(
    query: String = """
    SELECT DISTINCT ?concept 
    WHERE {
      ?s a ?concept 
    }  
  """) = {

    // TODO: SPARQL object

    val baseURI = "http://default/"
    val conn = repo.getConnection

    val dump = new StreamingOutput {
      def write(out: OutputStream) {
        try {

          conn.prepareTupleQuery(QueryLanguage.SPARQL, query, baseURI)
            .evaluate(new SPARQLResultsCSVWriter(out))

        } catch {
          case err: Throwable =>
            println(err.getStackTrace.mkString("\n"))
        }
        out.flush()
        out.close()
      }
    }

    conn.close()

    Response
      .ok()
      .entity(dump)
      .`type`("text/simple")
      .build()

  }

}


