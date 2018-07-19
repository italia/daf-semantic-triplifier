package triplifier.endpoints

import javax.inject.Inject
import javax.ws.rs.Path
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import io.swagger.annotations.Api
import it.almawave.kb.http.providers.ConfigurationService
import javax.ws.rs.core.Response
import java.util.Date

@Api(tags = Array("testing"))
@Path("/configuration")
class ConfigurationEndpoint {

  @Inject var _configuration: ConfigurationService = null

  @GET
  @Path("/show")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def show_configuration() = {

    Response
      .ok()
      .entity(_configuration.json)
      .`type`(MediaType.TEXT_PLAIN + "; charset=UTF-8") // CHECK: bodywriter per RDF...
      .encoding("UTF-8")
      .lastModified(new Date())
      .build()

  }

}