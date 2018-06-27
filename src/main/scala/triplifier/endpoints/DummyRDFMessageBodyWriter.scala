//package triplifier.endpoints
//
//import javax.ws.rs.ext.MessageBodyWriter
//import java.lang.annotation.Annotation
//import org.openrdf.model.Model
//import java.lang.reflect.Type
//import javax.ws.rs.core.MediaType
//import javax.ws.rs.core.MultivaluedMap
//import java.io.OutputStream
//import org.openrdf.rio.Rio
//import org.openrdf.rio.WriterConfig
//import org.openrdf.rio.RDFFormat
//
///*
// * CHECK: we could customize the response using an ad-hoc MessageBodyWriter for RDF models
// *
// * this is a naive ide for serializing an in-memory model
// */
//class DummyRDFMessageBodyWriter extends MessageBodyWriter[Model] {
//
//  def getSize(
//    model:       Model,
//    klass:       Class[_],
//    java_type:   Type,
//    annotations: Array[Annotation],
//    media_type:  MediaType): Long = model.size()
//
//  def isWriteable(
//    klass:       Class[_],
//    java_type:   Type,
//    annotations: Array[Annotation],
//    media_type:         MediaType): Boolean = false
//
//  def writeTo(
//    model:       Model,
//    klass:       Class[_],
//    java_type:   Type,
//    annotations: Array[Annotation],
//    media_type:  MediaType,
//    multi_map:   MultivaluedMap[String, Object],
//    out:         OutputStream) {
//
//    val _media = media_type.getType // CHECK: we should extend the available MIME types here
//
//    val _settings = new WriterConfig
//
//    Rio.write(model, out, RDFFormat.NTRIPLES, _settings)
//
//  }
//
//}
