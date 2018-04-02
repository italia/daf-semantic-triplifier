package examples

import it.unibz.inf.ontop.r2rml.R2RMLParser
import eu.optique.api.mapping.R2RMLMappingManager
//import eu.optique.api.mapping.MappingFactory
import eu.optique.r2rml.api.MappingFactory
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager
import org.openrdf.rio.Rio
import eu.optique.api.mapping.LogicalTable

object MainR2RMLParser extends App {

  val manager = RDF4JR2RMLMappingManager.getInstance

  val query_start = """
     SELECT id, denominazione_it FROM 'gove__amministrazione.default_org_o_anpr_archivio_storico_comuni'
  """

  val factory: eu.optique.r2rml.api.MappingFactory = manager.getMappingFactory

  val view = factory.createR2RMLView(query_start)

  println(view)

  val templs = factory.createTemplate("http://example/{id}")
  val sm = factory.createSubjectMap(templs)

  println(sm)

  //  factory.createPredicateObjectMap(arg0, arg1, arg2)

}