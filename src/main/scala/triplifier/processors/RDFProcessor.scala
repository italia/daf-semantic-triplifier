package triplifier.processors

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import scala.util.Try
import org.openrdf.model.Model
import org.openrdf.model.Statement
import java.io.OutputStream
import org.openrdf.rio.RDFFormat

object RDFProcessor {

  def ontop(config: Config) = new OntopProcessor(config)

}

/**
 * This is a naive contract for RDFProcessor, it's still unstable.
 * Currently the underlying implementation is based on ontop processor.
 *
 * TODO: update to RDF4J dependency
 */
trait RDFProcessor {

  def injectParameters(content: String, parameters: Config = ConfigFactory.empty()): String

  def loadTurtle(rdf_content: String, baseURI: String = "test://memory/"): Try[Model]

  def triplesMaps(r2rmlModel: Model): Try[Seq[String]]

  /**
   * This method executes an RDF process, using the given R2RML mapping,
   * and outputs a sequence of Statement, wrapped in a Try object
   */
  def process(r2rml: String): Try[Seq[Statement]]

  /**
   * The dump method is an extension of the process method, providing and explicit separation of
   * various elements, and the serialization using an output stream.
   */
  def dump(r2rml_list: Seq[String])(metadata: Option[String])(rdf_data: Option[String])(out: OutputStream, rdf_format: RDFFormat = RDFFormat.NTRIPLES)

  /**
   * the preview can be implemented in some different ways, but it's important to have it,
   * in order to be abel to give feedback to the
   */
  def previewDump(rdfFileName: String, offset: Int = -1, limit: Int = -1): String

}