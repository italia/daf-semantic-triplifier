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

import it.almawave.linkeddata.kb.utils.JSON
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

@Api(tags = Array("RDF processor"))
@Path("/triplify")
class StatelessEndpoint {

  private val logger = LoggerFactory.getLogger(this.getClass)

  @Context
  var uriInfo: UriInfo = null

  // TODO: handle errors

  @POST
  @Path("/process")
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.TEXT_PLAIN))
  def createRDFByMapping(
    @FormParam("config") config: String,
    @FormParam("r2rml") r2rml:   String,
    @FormParam("format") mime:   String,
    @Context req:                Request) = {

    val _config = URLDecoder.decode(config, "UTF-8")

    println("\n\n...............................")
    println("\n\nCONFIG" + _config)
    println("...............................\n\n")

    val rdf_format = getFormat(mime)

    println("\n\nrequested format: " + mime)
    println("requested format: " + rdf_format + "\n\n")
    println(s"R2RML mapping: ${r2rml}")

    val ontop = OntopProcessor.parse(config)

    val stream = new StreamingOutput {
      def write(out: OutputStream) {
        ontop.dump(r2rml, out, rdf_format)
        out.flush()
        out.close()
      }
    }

    //    throw new RuntimeException("VERIFY EXCEPTION HANDLING...")

    Response
      .ok()
      .entity(stream)
      .`type`(MediaType.TEXT_PLAIN) // CHECK: bodywriter per RDF...
      .encoding("UTF-8")
      .lastModified(new Date())
      .header("debug.configuration", _config.toString())
      .build()

    // ??   } recover { case err => err }

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





//  IDEA
//  @Path("/{datasetID}.{ext}")
//  def test_process(
//    @PathParam("datasetID") datasetID: String,
//    @PathParam("ext") ext:             String) = {
//    val base_uri = uriInfo.getBaseUri
