package experiments

import org.openrdf.rio.WriterConfig
import java.io.OutputStream
import org.openrdf.rio.turtle.TurtleWriter
import org.openrdf.rio.helpers.BasicWriterSettings
import org.openrdf.query.resultio.BasicQueryWriterSettings
import org.openrdf.rio.helpers.XMLWriterSettings

class RDF4JPrettyTurtleWriter(fos: OutputStream) extends TurtleWriter(fos) {

  import java.lang.{ Boolean => JBoolean }

  val config = new WriterConfig
  config.set[JBoolean](BasicWriterSettings.PRETTY_PRINT, true)
  config.set[JBoolean](BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL, true)
  config.set[JBoolean](BasicWriterSettings.RDF_LANGSTRING_TO_LANG_LITERAL, true)
  config.set[JBoolean](BasicQueryWriterSettings.ADD_SESAME_QNAME, false)
  config.set[JBoolean](XMLWriterSettings.INCLUDE_ROOT_RDF_TAG, true)
  config.set[JBoolean](BasicWriterSettings.RDF_LANGSTRING_TO_LANG_LITERAL, true)
  config.set[JBoolean](BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL, true)

  // TODO: externalize configs for namespaces
  this.handleNamespace("skos", "http://www.w3.org/2004/02/skos/core#")
  this.handleNamespace("l0", "https://w3id.org/italia/onto/l0/")
  this.handleNamespace("clvapit", "https://w3id.org/italia/onto/CLV/")
  this.handleNamespace("countries", "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/countries/")
  this.handleNamespace("regions", "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/regions/")
  this.handleNamespace("provinces", "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/provinces/")
  this.handleNamespace("cities", "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/cities/")
  this.handleNamespace("identifiers", "https://w3id.org/italia/controlled-vocabulary/identifiers/")

  this.setWriterConfig(config)

}