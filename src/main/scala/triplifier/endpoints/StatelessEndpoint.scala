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
import scala.util.parsing.json.JSON
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

@Api(tags = Array("RDF processor"))
@Path("/triplify")
class StatelessEndpoint {

  private val logger = LoggerFactory.getLogger(this.getClass)

  @Context
  var uriInfo: UriInfo = null

  // TODO: handle errors

  //CHECK file upload
  //@FormDataParam("r2rml") is:                  InputStream,
  //@FormDataParam("r2rml") header:              FormDataContentDisposition,

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

    val r2rml_input = r2rml

    val rdf_format = getFormat(mime)
    println("\n\nrequested format: " + mime)
    println("requested format: " + rdf_format + "\n\n")

    println(s"R2RML mapping: ${r2rml_input}")

    val ontop = OntopProcessor.parse(config)

    val mappings = List(R2RMLComuni.comuni_regioni)

    val stream = new StreamingOutput {
      def write(out: OutputStream) {
        ontop.dump(r2rml_input, out, rdf_format)
        out.flush()
        out.close()
      }
    }

    Response.ok().entity(stream).`type`(MediaType.TEXT_PLAIN).build()

  }

  def getFormat(mime: String) = {
    val format = URLDecoder.decode(mime, "UTF-8")
    Rio.getWriterFormatForMIMEType(format, RDFFormat.NTRIPLES)
  }

  def readInputstream(is: InputStream): String = {
    val src = Source.fromInputStream(is)("UTF-8")
    val content = src.getLines().mkString("\n")
    src.close()
    content
  }

  //  REVIEW
  //  @GET
  //  @Produces(Array(MediaType.TEXT_PLAIN))
  //  @ApiOperation(nickname = "toRDF", value = "test - stateless triplifier endpoint")
  //  @Path("/{datasetID}.{ext}")
  //  def test_process(
  //    @PathParam("datasetID") datasetID: String,
  //    @PathParam("ext") ext:             String) = {
  //
  //    val base_uri = uriInfo.getBaseUri
  //
  //    val dump_file = "./target/EXPORT/DUMP_comuni.nt"
  //
  //    val ontop = OntopProcessor.sqlite
  //
  //    val mappings = List(
  //      //      R2RMLComuni.comuni_comuni,
  //      //      R2RMLComuni.comuni_province,
  //      R2RMLComuni.comuni_regioni)
  //
  //    val rdf_format = Rio.getParserFormatForFileName(s"${datasetID}.${ext}")
  //    println("\n\n\n\nRDF FORMAT:" + rdf_format, datasetID, ext)
  //
  //    //    val r2rml_model = ontop.loadR2RMLString(mappings.mkString("\n"))
  //
  //    val stream = new StreamingOutput {
  //      def write(out: OutputStream) {
  //        ontop.dump(mappings, out, rdf_format)
  //        out.flush()
  //        out.close()
  //      }
  //    }
  //
  //    Response.ok().entity(stream).`type`(MediaType.TEXT_PLAIN).build()
  //
  //  }

}
