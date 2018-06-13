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

@Api(tags = Array("RDF processor"))
@Path("/triplify")
class RDFProcessEndpoint {

  private val logger = LoggerFactory.getLogger(this.getClass)

  @Context
  var uriInfo: UriInfo = null

  @GET
  @Path("/datasets/{user}/{group}/{dataset: .+?}.{ext}")
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.TEXT_PLAIN))
  def createRDFByMapping(
    @PathParam("user")@DefaultValue("opendata") user:                      String,
    @PathParam("group")@DefaultValue("territorial-classifications") group: String,
    @PathParam("dataset")@DefaultValue("regions") dataset:                 String,
    @PathParam("ext")@DefaultValue("ttl") ext:                             String,
    @Context req:                                                          Request) = {

    val fs = new FileDataSource(s"${user}/${group}/${dataset}")
    fs.getDump(ext)

    // TODO: a better error handling

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

class FileDataSource(path: String) {

  // TODO: configuration
  val base_path = "./data" // TODO: configure RDF storage

  val dataset_dir = Paths.get(base_path, s"${path}").toAbsolutePath().normalize()

  val config_path = Files.list(dataset_dir).iterator().toList.filter { f => f.toString().endsWith(".conf") }.head
  val config = Files.readAllLines(config_path).mkString("\n")

  val meta_path = Files.list(dataset_dir).iterator().toList.filter { f => f.toString().endsWith(".metadata.ttl") }.head
  val meta = Files.readAllLines(meta_path).mkString("\n")

  val r2rml_paths = Files.list(dataset_dir).iterator().toList.filter { f => f.toString().endsWith(".r2rml.ttl") }.toList
  val r2rmls = r2rml_paths.map { r2rml_path => Files.readAllLines(r2rml_path).mkString("\n") }.toList

  val ontop = OntopProcessor.parse(config)

  def getDump(ext: String) = {

    val rdf_format = Rio.getWriterFormatForFileName(s"${path}.${ext}", RDFFormat.TURTLE)
    val baos = new ByteArrayOutputStream
    ontop.dump(r2rmls)(Option(meta))(baos, rdf_format)
    val content = baos.toString()
    baos.close()
    content
  }

}


