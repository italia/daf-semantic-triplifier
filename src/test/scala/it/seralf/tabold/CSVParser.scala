package it.seralf.tabold

import java.net.URI
import it.seralf.tabold.helpers.UsingHelpers
import java.io.InputStream
import scala.util.Try
import scala.io.Source

import java.lang.{ Double => JDouble, Long => JLong }
import scala.collection.mutable.LinkedHashMap

object CSVParser {

  // TODO: enum with levels

  import UsingHelpers._
  //  import Table.Row

  val verbose = false
  val D = '"'
  val S = ','
  val rgx_split = s"""${S}(?=([^${D}]*"[^${D}]*${D})*[^${D}]*$$)"""

  // idea: creare un file CSV con un sottoinsieme dei dati (ed header!)
  def sample(is: InputStream, from: Int = -1, to: Int = -1) = {

    using(Source.fromInputStream(is)("UTF-8").getLines().toStream)(_ => {}) { src =>
      // HEADER ++ ROW
      src.zipWithIndex.slice(0, 1) ++ src.zipWithIndex.slice(from, to)
    }

  }

  case class CELL(name: String, `type`: String, value: Object)

  def parse(content: Seq[(String, Int)], withHeader: Boolean = true) = {

    using(content)(_ => {}) { in =>

      val headers_names = "_ROW_NUM" :: in.head._1.split(rgx_split).toList

      // what if the file is empty?
      val headers_types = "Long" :: (in.tail(0)._1).split(rgx_split).map(guess_type).map(_._1.getSimpleName).toList

      val headers: Seq[(String, String)] = headers_names.zip(headers_types)

      val rows = in.tail.map {
        case (line, i) =>

          val fields = new Integer(i) :: line.split(rgx_split).toList

          //          if (!verbose)
          //            headers_names.zip(fields)
          //          else
          //            headers.zip(fields).map {
          //              el => (el._1._1, el._1._2, el._2)
          //            }

          //          val doc = headers.zip(fields).map {
          //            el => (el._1._1, el._2)
          //          }
          //          LinkedHashMap(doc: _*)

          headers.zip(fields).map {
            el =>
              val _value = el._2 match {
                case txt: String => txt.replaceAll("\"(.*)\"", "$1")
                case x           => x
              }

              CELL(el._1._1, el._1._2, _value)
          }

      }

      rows

    }.get

  }

  def guess_type(item: String): (Class[_ <: Any], Double) = {

    import scala.util.control.Exception.allCatch

    // check Long or check Double or check URI or String
    def isLong(s: String): Boolean = (allCatch opt JLong.parseLong(s)).isDefined

    def isDouble(s: String): Boolean = (allCatch opt JDouble.parseDouble(s)).isDefined

    def isURI(s: String): Boolean = {
      s != null && !s.trim().equals("") &&
        s.contains(":") &&
        s.size > 3 &&
        Try { new URI(s) }.isSuccess
    }

    val klass: Class[_ <: Any] = if (isLong(item)) {
      classOf[Long]
    } else if (isDouble(item)) {
      classOf[Double]
    } else if (isURI(item)) {
      classOf[URI]
    } else {
      classOf[String]
    }

    (klass, 1.0) //

  }

}