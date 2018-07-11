package triplifier.endpoints

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
import triplifier.repository.MockRepo
import java.nio.file.Paths
import javax.inject.Inject
import javax.ws.rs.core.Context
import javax.ws.rs.QueryParam
import javax.ws.rs.DefaultValue
import io.swagger.models.parameters.BodyParameter
import javax.ws.rs.FormParam
import javax.ws.rs.Encoded
import io.swagger.models.parameters.FormParameter

@Api(tags = Array("SPARQL"))
@Path("/sparql")
class MockSPARQLEndpoint {

  @Inject var mock_service: RepoService = null

  // TODO: detailed exception handling
  // TODO: consider wrapping a SELECT * around the CONSTRUCTs...

  @POST
  def sparql(
    @DefaultValue("SELECT DISTINCT ?concept WHERE { ?s a ?concept }") query: String,
    @DefaultValue("csv")@QueryParam("format") format:                        String) = {

    println("\nMOCK2 @ " + mock_service.hashCode())

    val sparql_results = new StreamingOutput {
      def write(out: OutputStream) {
        mock_service.repo.endpoint.query(query, format)(out)
        out.flush()
      }
    }

    Response
      .ok()
      .entity(sparql_results)
      .`type`("text/simple")
      .build()

  }

}


