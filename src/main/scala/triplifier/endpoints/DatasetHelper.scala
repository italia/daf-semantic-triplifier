package triplifier.endpoints

import java.nio.file.Files
import java.nio.file.Paths
import triplifier.processors.OntopProcessor
import java.io.ByteArrayOutputStream
import org.openrdf.rio.RDFFormat
import org.openrdf.rio.Rio

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import java.nio.charset.Charset
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/*
 *  TODO: review of DatasetHelper logic, refactorization
 */
class DatasetHelper(base_path: String, path: String) {

  val ENCODING = "UTF-8"
  val CHARSET = Charset.forName(ENCODING)

  val dataset_dir = Paths.get(base_path, s"${path}").toAbsolutePath().normalize()

  val config_path = Files.list(dataset_dir).iterator().toList.filter { f => f.toString().endsWith(".conf") }.head
  val configTxt = Files.readAllLines(config_path, CHARSET).mkString("\n")
  val config = ConfigFactory.parseString(configTxt).resolve()
  // CHECK: how can we re-use default configs here?

  val meta_path = Files.list(dataset_dir).iterator().toList.filter { f => f.toString().endsWith(".metadata.ttl") }.head
  val meta = Files.readAllLines(meta_path, CHARSET).mkString("\n")

  val r2rml_paths = Files.list(dataset_dir).iterator().toList.filter { f => f.toString().endsWith(".r2rml.ttl") }.toList
  val r2rmls = r2rml_paths.map { r2rml_path => Files.readAllLines(r2rml_path, CHARSET).mkString("\n") }.toList

  val ontop = OntopProcessor(config)

  def createRDFDump(ext: String): String = {

    val rdf_format = Rio.getWriterFormatForFileName(s"${path}.${ext}", RDFFormat.TURTLE)
    val baos = new ByteArrayOutputStream
    ontop.dump(r2rmls)(Option(meta))(baos, rdf_format)
    val content = baos.toString(ENCODING)
    baos.close()
    content

  }

}


