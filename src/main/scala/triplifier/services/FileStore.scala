package triplifier.services

import java.nio.file.FileSystems
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.nio.file.Files

object FileStore {

  import scala.collection.JavaConversions._
  import scala.collection.JavaConverters._

  def getPathList(dir_path: Path): Seq[Path] = {

    Stream(dir_path.toAbsolutePath().normalize()) ++ (if (!Files.isDirectory(dir_path))
      Nil
    else
      Files.list(dir_path).iterator().toStream.flatMap { p => getPathList(p) })

  }
}

class FileStore(rootFolderName: String) {

  val fs = FileSystems.getDefault

  val logger = LoggerFactory.getLogger(this.getClass)

  val root_path = fs.getPath(rootFolderName).toAbsolutePath().normalize()
  println("ROOT? " + root_path)

  def getPath() = {

    //    Paths.get(x$1, x$2)

    //     val file = Paths.get(s"${r2rml_base_path}/${group}/${dataset_path}/${fragment}.r2rml.ttl")
    //      .toAbsolutePath().normalize().toFile()

    //    s"${r2rml_base_path}/${group}/${dataset_path}/${fragment}.r2rml.ttl"
  }

}

//TODO: check https://docs.oracle.com/javase/8/docs/api/index.html

