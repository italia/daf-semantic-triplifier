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
import triplifier.processors.RDFProcessor
import java.io.OutputStream
import java.nio.file.Path
import java.io.FileOutputStream
import org.slf4j.LoggerFactory
import scala.util.Random
import java.net.URI
import java.io.File

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.util.Try

class DatasetsStore(default_configuration: Config) {

  val logger = LoggerFactory.getLogger(this.getClass)

  val r2rml_base_path: String = default_configuration.getString("r2rml.path")
  val dir_path = Paths.get(r2rml_base_path)

  def datasetsPathsList() = {

    FileStore.getPathList(dir_path)
      .filter { p => p.toString().contains("r2rml") }
      .map { p => p.getParent }

  }

  def datasets = {

    datasetsPathsList.map { p =>

      val local: String = p.toString().replace(r2rml_base_path.toString(), "")
      new SingleDatasetStore(default_configuration, local)

    }

  }

}

/*
 * 	TODO: refactorize this class, isolating the RDF process
 *  TODO: review of DatasetHelper logic, refactorization
 *  TODO: DatasetsService
 */
class SingleDatasetStore(default_configuration: Config, path: String) {

  val logger = LoggerFactory.getLogger(this.getClass)

  val ENCODING = "UTF-8"
  val CHARSET = Charset.forName(ENCODING)

  val r2rml_base_path: String = default_configuration.getString("r2rml.path")
  val rdf_dump_dir: String = default_configuration.getString("datasets.rdf.dump.path")

  // each dataset will be related to an actual local path, from which the configs are loaded...
  val dataset_dir = Paths.get(r2rml_base_path, s"${path}").toAbsolutePath().normalize()

  // REVIEW the chain here
  lazy val config = this.getDatasetConfig().get
  lazy val r2rmls = this.getR2RMLMappinglList().map(_._2)
  lazy val meta: Option[String] = this.getMetadata()
  lazy val rdf_extra: Option[String] = this.getAdditionalRDFData() //this.getAdditionalRDFData()

  // ideally, here we could change RDF processor by configuration, at some point
  val rdf_processor: RDFProcessor = RDFProcessor.ontop(config)

  def writeRDFDump(dump_path: Path)(out: OutputStream) {
    logger.debug(s"RDF> creating dump for ${dump_path}")
    val rdf_format = Rio.getWriterFormatForFileName(s"${dump_path}", RDFFormat.TURTLE)
    rdf_processor.dump(r2rmls)(meta)(rdf_extra)(out, rdf_format)
  }

  /**
   * this method loads a pre-processed RDF dump, or part of it, as requested
   */
  def previewAsLines(path: Path)(from: Int = 0, until: Int = Int.MaxValue): Seq[String] = {

    // TODO: random sub-sequence
    // TODO: add a preview as RDF feature...
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

  /*
   * This method loads a configuration oobject for the dataset
   */
  def getDatasetConfig(): Try[Config] = Try {

    // TODO: fallback with default parent config

    val config_path = FileStore.getPathList(dataset_dir)
      .filter { f => f.toString().endsWith(".conf") }
      .head

    val configTxt = Files.readAllLines(config_path, CHARSET).mkString("\n")
    val config = ConfigFactory.parseString(configTxt)

    config
      .withFallback(default_configuration.getConfig("jdbc").atPath("jdbc")) // CHECK if some different key is needed
      .resolve() // TODO: add default configuration values?

  }

  def readFile(path: Path): Try[String] = Try {

    if (!Files.exists(path)) throw new RuntimeException(s"path ${path} not found!")
    Files.readAllLines(path, CHARSET).mkString("\n")

  }

  def saveFile(path: Path, content: String): Try[Path] = Try {

    val file_path = path.toAbsolutePath().normalize()
    val dir = file_path.getParent.toFile()

    if (!dir.exists()) dir.mkdirs()

    logger.debug(s"writing ${file_path}")

    Files.write(file_path, content.getBytes)

    file_path

  }

  def getR2RMLMappinglList(): Seq[(URI, String)] = {
    val r2rml_paths = FileStore.getPathList(dataset_dir).filter { f => f.toString().endsWith(".r2rml.ttl") }.toList
    val r2rmls = r2rml_paths.map { r2rml_path =>
      (r2rml_path.toUri(), readFile(r2rml_path).getOrElse("# no content"))
    }.toList
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

object MainSingleDatasetStore extends App {

  val group = "daf"
  val dataset = "ipa/indirizzi"
  val conf = ConfigFactory.parseFile(new File("conf/triplifier.conf"))
  val fs = new SingleDatasetStore(conf, s"${group}/${dataset}")

  FileStore.getPathList(Paths.get("data/daf"))
    .filter { p => p.toString().contains("r2rml") }
    .foreach { path =>
      println(path)
    }

  fs.saveFile(Paths.get("../testing/another_one.r2rml.ttl"), "testing.....").get

  new DatasetsStore(conf)

}




