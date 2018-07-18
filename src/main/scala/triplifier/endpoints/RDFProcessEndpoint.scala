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
import java.nio.file.Paths
import javax.ws.rs.DefaultValue
import java.nio.file.Files

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import javax.ws.rs.Encoded
import javax.inject.Inject
import it.almawave.kb.http.providers.ConfigurationService
import io.swagger.annotations.ExternalDocs
import java.nio.charset.Charset
import javax.ws.rs.QueryParam
import triplifier.services.SingleDatasetStore
import scala.util.Random
import java.io.OutputStreamWriter
import java.io.BufferedWriter

@Api(tags = Array("RDF processor"))
@Path("/triplify")
class RDFProcessEndpoint {

  private val logger = LoggerFactory.getLogger(this.getClass)

  @Inject var _configuration: ConfigurationService = null

  /*
   * TODO: add content-negotiation for handling an HTML representation of the data
   * TODO: a better error handling
   * TODO: process by stream
   */
  @GET
  @Path("/datasets/{group}/{dataset: .+?}.{ext}")
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.TEXT_PLAIN))
  @ExternalDocs(
    value = "endpoint for local testing",
    url = "/kb/api/v1/triplify/datasets/test/territorial-classifications/regions.ttl")
  def createRDFByMapping(
    @PathParam("group")@DefaultValue("test") group:                                    String,
    @PathParam("dataset")@DefaultValue("territorial-classifications/regions") dataset: String,
    @PathParam("ext")@DefaultValue("ttl") ext:                                         String,
    @QueryParam("cached")@DefaultValue("false") cached:                                Boolean,
    @Context req:                                                                      Request) = {

    // loading default, general configuration
    val conf = _configuration.conf

    //    logger.debug(s"\n\nDEBUG> analyzing request.........................................")
    //    logger.debug(s"GROUP: ${group}")
    //    logger.debug(s"DATASET: ${dataset}")
    //    logger.debug(s"EXT: ${ext}")

    val fs = new SingleDatasetStore(conf, s"${group}/${dataset}")

    val dump_path = fs.getRDFDumpPath(ext)

    if (!cached) {
      // save DUMP su file
      fs.saveRDFDump(dump_path)
      logger.debug(s"RDF> saved dump file ${dump_path}")
    } else {
      logger.warn(s"RDF> using cached dump file ${dump_path}")
    }

    // loading preview
    val _preview = fs.previewAsLines(dump_path)()

    Response
      .ok()
      .entity(LinesResponseStreaming(_preview: _*))
      .`type`(MediaType.TEXT_PLAIN + "; charset=UTF-8") // CHECK: bodywriter per RDF...
      .encoding("UTF-8")
      .lastModified(new Date())
      .build()

    // TODO: improve RDF/R2RML parsing with more efficient exception support

  }

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

object LinesResponseStreaming {
  def apply(lines: String*) = {
    new StreamingOutput() {
      override def write(out: OutputStream) {
        lines.foreach { line =>
          out.write(line.getBytes)
          out.write("\n".getBytes)
        }
        out.flush()
      }
    }
  }
}
