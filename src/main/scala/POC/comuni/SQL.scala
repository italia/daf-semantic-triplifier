package POC.comuni

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.charset.Charset

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object SQL {

  def fromFile(path: String): String =
    Files.readAllLines(Paths.get(path), Charset.forName("UTF-8")).mkString("\n")

}