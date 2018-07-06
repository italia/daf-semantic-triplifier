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
import triplifier.processors.OntopProcessor
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
import io.swagger.annotations.ExternalDocs

import javax.inject.Inject
import it.almawave.kb.http.providers.ConfigurationService

@Api(tags = Array("RDF processor"))
@Path("/triplify")
class StatelessEndpoint {

  private val logger = LoggerFactory.getLogger(this.getClass)

  @Inject var _configuration: ConfigurationService = null

  @POST
  @Path("/stateless/process")
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.TEXT_PLAIN))
  @ExternalDocs(
    value = "\t\tRDF process form\n",
    url = "/static/rdf-process.html")
  def createRDFByMapping(
    @FormParam("config") configTxt:  String,
    @FormParam("r2rml") r2rml:       String,
    @FormParam("metadata") metadata: String,
    @FormParam("rdf_data") rdf_data: String,
    @FormParam("name") fileName:     String,
    @FormParam("format") mime:       String,
    @Context req:                    Request) = {

    Future {

      val _config = URLDecoder.decode(configTxt, "UTF-8")
      val config = ConfigFactory.parseString(_config)
        .withFallback(_configuration.conf)
        .resolve()

      logger.debug(s"\n\nusing configuration:\n${_config}")

      val rdf_format = getFormat(mime)

      val meta_opt = Option(metadata)

      logger.debug("requested format: " + rdf_format + "\n\n")
      logger.debug(s"R2RML mapping: ${r2rml}")

      //      val ontop = OntopProcessor(config)
      //      val baos = new ByteArrayOutputStream
      //      ontop.dump(List(r2rml))(meta_opt)(None)(baos, rdf_format)
      //      val dump = baos.toString("UTF-8")

      // TODO: refactorize the streaming output for ontop
      val ontop = OntopProcessor(config)
      val dump = new StreamingOutput {
        def write(out: OutputStream) {
          try {
            ontop.dump(List(r2rml))(meta_opt)(None)(out, rdf_format)
          } catch {
            case err: Throwable =>
              logger.error(err.getStackTrace.mkString("\n"))
          }
          out.flush()
          out.close()
        }
      }

      Response
        .ok()
        .entity(dump)
        .`type`(MediaType.TEXT_PLAIN + "; charset=UTF-8") // CHECK: bodywriter per RDF...
        .encoding("UTF-8")
        .lastModified(new Date())
        .header("debug.configuration", _config.toString())
        .header("content-disposition", s"attachment; filename = ${fileName}.${rdf_format.getDefaultFileExtension}")
        .build()

    }.await

  }

  // REVIEW
  def getFormat(mime: String) = {

    val format = URLDecoder.decode(mime, "UTF-8")
    val rdf_format = Rio.getWriterFormatForMIMEType(format, RDFFormat.NTRIPLES)
    logger.debug(s"RDF format for ${format} is ${rdf_format}")
    rdf_format

  }

  def readInputstream(is: InputStream): String = {
    val src = Source.fromInputStream(is)("UTF-8")
    val content = src.getLines().mkString("\n")
    src.close()
    content
  }

}