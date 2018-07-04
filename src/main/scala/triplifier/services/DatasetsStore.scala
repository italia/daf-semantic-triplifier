package triplifier.services

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
class DatasetsStore(default_configuration: Config, path: String) {

  val logger = LoggerFactory.getLogger(this.getClass)

  val ENCODING = "UTF-8"
  val CHARSET = Charset.forName(ENCODING)

  val r2rml_base_path: String = default_configuration.getString("r2rml.path")
  val rdf_dump_dir: String = default_configuration.getString("datasets.rdf.dump.path")

  // each dataset will be related to an actual local path, from which the configs are loaded...
  val dataset_dir = Paths.get(r2rml_base_path, s"${path}").toAbsolutePath().normalize()

  val config = this.getDatasetConfig()
  val r2rmls = this.getR2RMLMappinglList()
  val meta: Option[String] = this.getMetadata()
  val rdf_extra: Option[String] = this.getAdditionalRDFData() //this.getAdditionalRDFData()

  val rdf_processor: RDFProcessor = OntopProcessor(config)

  def writeRDFDump(dump_path: Path)(out: OutputStream) {
    logger.debug(s"RDF> creating dump for ${dump_path}")
    val rdf_format = Rio.getWriterFormatForFileName(s"${dump_path}", RDFFormat.TURTLE)
    rdf_processor.dump(r2rmls)(meta)(rdf_extra)(out, rdf_format)
  }

  /**
   * this method loads a pre-processed RDF dump, or part of it, as requested
   */
  def previewAsLines(path: Path)(from: Int = 0, until: Int = Int.MaxValue): Seq[String] = {
    Files
      .readAllLines(path, Charset.forName("UTF-8"))
      .toStream
      .slice(from, until)
  }

  // TODO: save configs

  def getRDFDumpPath(ext: String): Path = {
    Paths.get(rdf_dump_dir, s"${path}.${ext}").toAbsolutePath().normalize()
  }

  def saveRDFDump(dump_path: Path): Path = {

    logger.debug(s"RDF> saving file ${dump_path}")

    val dump_folder = dump_path.toFile().getParentFile
    if (!dump_folder.exists()) dump_folder.mkdirs()
    val fos = new FileOutputStream(dump_path.toFile())
    this.writeRDFDump(dump_path)(fos)
    fos.close()

    dump_path
  }

  def createRDFDumpAsString(dump_path: Path): String = {

    val rdf_format = Rio.getWriterFormatForFileName(s"${dump_path}", RDFFormat.TURTLE)
    val baos = new ByteArrayOutputStream
    writeRDFDump(dump_path)(baos)
    val content = baos.toString(ENCODING)
    baos.close()
    content

  }

  def getDatasetConfig(): Config = {

    // TODO: fallback with default parent config

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

  def getMetadata(): Option[String] = {

    Files.list(dataset_dir).iterator().toList
      .filter { f => f.toString().endsWith(".metadata.ttl") }
      .map { p => Files.readAllLines(p, CHARSET).mkString("\n") }
      .headOption
  }

  def getAdditionalRDFData(): Option[String] = {

    Files.list(dataset_dir).iterator().toList
      .filter { p => p.toString().matches(".*.ttl") }
      .filterNot { p => p.toString().matches(".*\\.r2rml\\..*") }
      .filterNot { p => p.toString().matches(".*\\.metadata\\..*") }
      .map { rdf_extra =>
        Files.readAllLines(rdf_extra, CHARSET).mkString("\n")
      }.headOption

  }

}



