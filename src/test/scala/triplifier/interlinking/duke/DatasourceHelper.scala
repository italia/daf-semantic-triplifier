package triplifier.interlinking.duke

import scala.collection.mutable.LinkedHashMap
import no.priv.garshol.duke.Record
import no.priv.garshol.duke.DataSource

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object DataSourceHelper {

  def lookupByID(src: DataSource, field: String, value: Object): Map[String, Any] = {
    this.getRecords(src)
      .find { doc =>
        doc.getOrElse(field, "").equals(value)
      }.getOrElse(Map())
  }

  def getRecords(src: DataSource): Stream[Map[String, Any]] = {
    src.getRecords.toStream
      .map { record => this.toMap(record) }
  }

  def toMap(record: Record): Map[String, Any] = {
    val doc = record.getProperties.map { name => (name, record.getValue(name)) }.toStream
    LinkedHashMap(doc: _*).toMap
  }

}