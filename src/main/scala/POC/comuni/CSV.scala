package POC.comuni

object CSV {

  def apply(separator: String, delimiter: String) = new CSV(separator, delimiter)

}

class CSV(separator: String, delimiter: String) {

  import java.util.{ Map => JMap }

  def writeAsString(rows: Seq[Seq[(String, Object)]]): Seq[String] = {

    // the first row is an header
    val header: List[String] = rows.head.map(_._1).toList

    // ...then the actual data
    val lines = rows.toStream
      .map(_.toList.map(_._2))
      .map { cells =>

        cells.map {
          case txt: String => s"${delimiter}${txt}${delimiter}"
          case null        => ""
          case obj         => obj.toString()
        }

      }

    (Stream(header) ++ lines)
      .map(line => line.mkString(separator))

  }

}