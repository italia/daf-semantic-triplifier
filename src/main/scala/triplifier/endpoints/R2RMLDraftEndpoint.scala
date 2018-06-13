package triplifier.endpoints

import java.time.LocalTime
import io.swagger.annotations.Api
import javax.ws.rs.Path
import javax.ws.rs.GET
import javax.ws.rs.Produces
import io.swagger.annotations.ApiOperation
import javax.ws.rs.core.MediaType
import org.slf4j.LoggerFactory
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo
import javax.ws.rs.core.Request
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.ZoneId
import org.openrdf.rio.RDFFormat
import java.io.OutputStream
import javax.ws.rs.core.StreamingOutput
import javax.ws.rs.core.Response
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.File
import java.io.InputStream
import java.io.FileOutputStream
import org.openrdf.rio.Rio
import javax.ws.rs.PathParam
import javax.ws.rs.Consumes
import org.glassfish.jersey.media.multipart.FormDataParam
import scala.io.Source
import javax.ws.rs.POST
import org.glassfish.jersey.media.multipart.FormDataContentDisposition

import java.net.URLDecoder
import javax.ws.rs.core.MultivaluedMap
import com.typesafe.config.Config
import javax.ws.rs.ext.MessageBodyReader
import java.lang.annotation.Annotation
import java.lang.reflect.Type

import com.typesafe.config.ConfigFactory
import io.swagger.models.parameters.BodyParameter
import javax.ws.rs.FormParam
import java.util.Date
import javax.ws.rs.ext.Provider
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.NotFoundException
import scala.util.Try
import javax.ws.rs.container.Suspended
import javax.ws.rs.container.AsyncResponse
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import scala.concurrent.ExecutionContext.Implicits.global
import it.almawave.kb.http.utils.AsyncHelper._
import triplifier.processors.RDFMapper

@Api(tags = Array("R2RMl"))
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
