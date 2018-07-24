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
import java.nio.file.Paths
import javax.inject.Inject
import javax.ws.rs.core.Context
import javax.ws.rs.QueryParam
import javax.ws.rs.DefaultValue
import io.swagger.models.parameters.BodyParameter
import javax.ws.rs.FormParam
import javax.ws.rs.Encoded
import io.swagger.models.parameters.FormParameter
import io.swagger.models.Operation
import io.swagger.annotations.ApiOperation

@Api(tags = Array("SPARQL"))
@Path("/repository")
class MockSPARQLEndpoint {

  @Inject var mock_service: RepoService = null

  // TODO: detailed exception handling
  // TODO: consider wrapping a SELECT * around the CONSTRUCTs...

  @POST
  @Path("/update")
  def update(query: String) {

    mock_service.repo.endpoint.update(query)

  }

  @POST
  @ApiOperation(
    value = "sparql endpoint for testing",
    notes = "this is a minimal sparql endpoint from in-memory data, for testing")
  @Path("/sparql")
  def sparql(
    @BodyParameter @DefaultValue("SELECT DISTINCT ?concept WHERE { ?s a ?concept }") query:String,
    @DefaultValue("csv")@QueryParam("format") format:                                      String) = {

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


