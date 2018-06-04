package triplifier.endpoints

import javax.ws.rs.ext.MessageBodyWriter
import org.openrdf.model.Model

class DummyRDFMessageBodyWriter extends MessageBodyWriter[Model] {

  def getSize(
    model: Model,
    x$2:   Class[_],
    x$3:   java.lang.reflect.Type,
    x$4:   Array[java.lang.annotation.Annotation],
    x$5:   javax.ws.rs.core.MediaType): Long = model.size()

  def isWriteable(
    x$1: Class[_],
    x$2: java.lang.reflect.Type,
    x$3: Array[java.lang.annotation.Annotation], x$4: javax.ws.rs.core.MediaType): Boolean = false

  def writeTo(
    model: org.openrdf.model.Model,
    x$2:   Class[_], x$3: java.lang.reflect.Type,
    x$4: Array[java.lang.annotation.Annotation],
    x$5: javax.ws.rs.core.MediaType,
    x$6: javax.ws.rs.core.MultivaluedMap[String, Object],
    x$7: java.io.OutputStream) {

  }

}