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
import triplifier.processors.RDFProcessor
import java.io.OutputStream

/*
 *  TODO: review of DatasetHelper logic, refactorization
 */
class DatasetHelper(default_configuration: Config, path: String) {

  val ENCODING = "UTF-8"
  val CHARSET = Charset.forName(ENCODING)

  val base_path: String = default_configuration.getString("r2rml.localPath")

  // each dataset will be related to an actual local path, from which the configs are loaded...
  val dataset_dir = Paths.get(base_path, s"${path}").toAbsolutePath().normalize()

  val meta = this.getMetadata()

  val r2rmls = this.getR2RMLMappinglList()

  val config = this.getDatasetConfig()

  val rdf_processor: RDFProcessor = OntopProcessor(config)

  def writeRDFDump(ext: String, out: OutputStream) {
    val rdf_format = Rio.getWriterFormatForFileName(s"${path}.${ext}", RDFFormat.TURTLE)
    rdf_processor.dump(r2rmls)(Option(meta))(out, rdf_format)
  }

  def createRDFDumpAsString(ext: String): String = {

    val rdf_format = Rio.getWriterFormatForFileName(s"${path}.${ext}", RDFFormat.TURTLE)
    val baos = new ByteArrayOutputStream
    rdf_processor.dump(r2rmls)(Option(meta))(baos, rdf_format)
    val content = baos.toString(ENCODING)
    baos.close()
    content

  }

  def getDatasetConfig(): Config = {

    val config_path = Files.list(dataset_dir).iterator().toList.filter { f => f.toString().endsWith(".conf") }.head
    val configTxt = Files.readAllLines(config_path, CHARSET).mkString("\n")
    val config = ConfigFactory.parseString(configTxt)

    config
      .withFallback(default_configuration.getConfig("jdbc").atPath("jdbc")) // CHECK if some different key is needed
      .resolve() // TODO: add default configuration values?

  }

  def getR2RMLMappinglList() = {
    val r2rml_paths = Files.list(dataset_dir).iterator().toList.filter { f => f.toString().endsWith(".r2rml.ttl") }.toList
    val r2rmls = r2rml_paths.map { r2rml_path => Files.readAllLines(r2rml_path, CHARSET).mkString("\n") }.toList
    r2rmls
  }

  def getMetadata(): String = {
    val meta_path = Files.list(dataset_dir).iterator().toList.filter { f => f.toString().endsWith(".metadata.ttl") }.head
    val meta = Files.readAllLines(meta_path, CHARSET).mkString("\n")
    meta
  }

}



