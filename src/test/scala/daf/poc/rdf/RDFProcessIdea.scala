package daf.poc.rdf

import java.io.OutputStream
import org.openrdf.model.Statement

trait RDFProcessIdea {

  def process(r2rml: String, out: OutputStream): Seq[Statement]

}