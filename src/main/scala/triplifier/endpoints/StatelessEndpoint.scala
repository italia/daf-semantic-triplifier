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

@Api(tags = Array("RDF processor"))
@Path("/triplify")
class StatelessEndpoint {

  private val logger = LoggerFactory.getLogger(this.getClass)

  @Context
  var uriInfo: UriInfo = null

  @GET
  @Produces(Array(MediaType.TEXT_PLAIN))
  @ApiOperation(nickname = "toRDF", value = "stateless triplifier endpoint")
  @Path("/{datasetID}.{ext}")
  def process(
    @PathParam("datasetID") datasetID: String,
    @PathParam("ext") ext:             String) = {

    val base_uri = uriInfo.getBaseUri

    val dump_file = "./target/EXPORT/DUMP_comuni.nt"

    val ontop = OntopProcessor.sqlite

    val mappings = List(
      //      R2RMLComuni.comuni_comuni,
      //      R2RMLComuni.comuni_province,
      R2RMLComuni.comuni_regioni)

    val rdf_format = Rio.getParserFormatForFileName(s"${datasetID}.${ext}")
    println("\n\n\n\nRDF FORMAT:" + rdf_format, datasetID, ext)

    val r2rml_model = ontop.loadR2RMLString(mappings.mkString("\n"))

    val stream = new StreamingOutput {
      def write(out: OutputStream) {
        ontop.dump(mappings, out, rdf_format)
        out.flush()
        out.close()
      }
    }

    //    val baos = new ByteArrayOutputStream
    //    ontop.dump(mappings, baos, RDFFormat.TURTLE)
    //    baos.flush()
    //
    //    Response.ok(baos.toString()).build()

    Response.ok().entity(stream).`type`(MediaType.TEXT_PLAIN).build()

  }

  def pipe(is: InputStream, os: OutputStream) {

    val buffer = Array[Byte]()
    var n = 0
    while (n > -1) {

      n = is.read()
      os.write(buffer, 0, n)

    }

    os.close();
  }

}

