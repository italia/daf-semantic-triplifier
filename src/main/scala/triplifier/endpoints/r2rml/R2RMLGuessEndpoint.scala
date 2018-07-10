package triplifier.endpoints.r2rml

import io.swagger.annotations.Api
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import org.slf4j.LoggerFactory
import javax.ws.rs.core.Response
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import java.net.URLDecoder
import com.typesafe.config.ConfigFactory
import javax.ws.rs.FormParam
import java.util.Date
import it.almawave.kb.http.utils.AsyncHelper._
import triplifier.processors.RDFMapper

@Api(tags = Array("R2RML draft"))
@Path("/triplification")
class R2RMLDraftEndpoint {

  private val logger = LoggerFactory.getLogger(this.getClass)

  @POST
  @Path("/r2rml/guess")
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.TEXT_PLAIN))
  def draft(
    @FormParam("config") config: String,
    @FormParam("query") query:   String) = {

    val _config = URLDecoder.decode(config, "UTF-8")

    val conf = ConfigFactory.parseString(_config).resolve()

    val _query = URLDecoder.decode(query, "UTF-8")

    val base_uri = if (conf.hasPath("parameters.baseURI")) conf.getString("parameters.baseURI") else "http://example"

    val test = new RDFMapper(conf)
    test.start()
    val r2rml = test.generate_mapping(_query, base_uri)
    test.stop()

    Response
      .ok()
      .entity(r2rml)
      .`type`(MediaType.TEXT_PLAIN) // CHECK: bodywriter per RDF...
      .encoding("UTF-8")
      .lastModified(new Date())
      .header("debug.configuration", _config.toString())
      // CHECK: .header("content-disposition", s"attachment; filename = ${fileName}.${rdf_format.getDefaultFileExtension}")
      .build()

  }

}
