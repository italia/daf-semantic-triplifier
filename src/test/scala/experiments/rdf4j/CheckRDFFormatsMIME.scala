package experiments.rdf4j

import org.openrdf.rio.RDFFormat
import org.junit.Test
import org.openrdf.rio.Rio
import org.junit.Assert

object CheckRDFFormats extends App {

  val formats = List(

    RDFFormat.TURTLE,
    RDFFormat.NTRIPLES,
    RDFFormat.RDFXML,
    RDFFormat.JSONLD,
    RDFFormat.NQUADS,
    RDFFormat.N3,
    RDFFormat.TRIX,
    RDFFormat.TRIG,
    RDFFormat.BINARY,
    RDFFormat.RDFJSON,
    RDFFormat.RDFA)

  formats.foreach { format =>

    println(format)

  }

}

class TestingRDFFormats {

  @Test
  def test_nt_mime() {
    val f_ok = Rio.getParserFormatForMIMEType("text/plain; ext=nt")
    Assert.assertEquals("nt", f_ok.getDefaultFileExtension)
    val f_no = Rio.getParserFormatForMIMEType("text/plain")
    Assert.assertEquals("nt", f_no.getDefaultFileExtension)
  }
  
  @Test
  def test_ttl_mime() {
    val f_ok = Rio.getParserFormatForMIMEType("text/turtle")
    Assert.assertEquals("ttl", f_ok.getDefaultFileExtension)
    val f_no = Rio.getParserFormatForMIMEType("text/plain; ext:ttl")
    Assert.assertEquals("nt", f_no.getDefaultFileExtension)
  }
  

}