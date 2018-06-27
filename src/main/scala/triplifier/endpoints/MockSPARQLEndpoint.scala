package triplifier.endpoints

import io.swagger.annotations.Api
import javax.ws.rs.Path
import javax.ws.rs.core.Response
import javax.ws.rs.GET

@Api(tags = Array("RDF processor"))
@Path("/sparql")
class MockSPARQLEndpoint {

  @GET
  def sparql() = {

    // TODO: SPARQL object
    
    Response
      .ok()
      .entity("NOT YET IMPLEMENTED")
      .`type`("text/simple")
      .build()

  }

}