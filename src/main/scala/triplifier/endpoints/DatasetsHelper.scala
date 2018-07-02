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
import java.nio.file.Path
import java.io.FileOutputStream
import org.slf4j.LoggerFactory

/*
 *  TODO: review of DatasetHelper logic, refactorization
 *  TODO: DatasetsService
 */
class DatasetHelper(default_configuration: Config, path: String) {

  val logger = LoggerFactory.getLogger(this.getClass)

  val ENCODING = "UTF-8"
  val CHARSET = Charset.forName(ENCODING)

  val r2rml_base_path: String = default_configuration.getString("r2rml.path")
  val rdf_dump_dir: String = default_configuration.getString("datasets.rdf.dump.path")

  // each dataset will be related to an actual local path, from which the configs are loaded...
  val dataset_dir = Paths.get(r2rml_base_path, s"${path}").toAbsolutePath().normalize()

  val config = this.getDatasetConfig()
  val r2rmls = this.getR2RMLMappinglList()
  val meta = this.getMetadata()
  val rdf_extra = this.getAdditionalRDFData() //this.getAdditionalRDFData()

  val rdf_processor: RDFProcessor = OntopProcessor(config)

  def writeRDFDump(ext: String)(out: OutputStream) {
    logger.debug(s"RDF> creating dump for ${path}.${ext}")
    val rdf_format = Rio.getWriterFormatForFileName(s"${path}.${ext}", RDFFormat.TURTLE)
    rdf_processor.dump(r2rmls)(Option(meta))(Option(rdf_extra))(out, rdf_format)
  }

  def previewAsLines(path: Path): Seq[String] = {
    Files.readAllLines(path, Charset.forName("UTF-8")).toStream
  }

  // TODO: save configs

  def saveRDFDump(ext: String): Path = {

    val dump_path = Paths.get(rdf_dump_dir, s"${path}.${ext}").toAbsolutePath().normalize()
    logger.debug(s"RDF> saving file ${dump_path}")

    val dump_folder = dump_path.toFile().getParentFile
    if (!dump_folder.exists()) dump_folder.mkdirs()
    val fos = new FileOutputStream(dump_path.toFile())
    this.writeRDFDump(ext)(fos)
    fos.close()

    dump_path
  }

  def createRDFDumpAsString(ext: String): String = {

    val rdf_format = Rio.getWriterFormatForFileName(s"${path}.${ext}", RDFFormat.TURTLE)
    val baos = new ByteArrayOutputStream
    writeRDFDump(ext)(baos)
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

  def getAdditionalRDFData(): String = {

    Files.list(dataset_dir).iterator().toList
      .filterNot { p => p.toString().matches(".*?(.*?)\\.ttl") }
      .map { rdf_extra =>
        Files.readAllLines(rdf_extra, CHARSET).mkString("\n")
      }.mkString("\n")

  }

}



