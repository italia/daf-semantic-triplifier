//package experiments.ontop.rdf4j
//
//import java.io.File
//
//import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration
//import it.unibz.inf.ontop.rdf4j.repository.OntopRepository
//import org.eclipse.rdf4j.repository.Repository
//import org.semanticweb.owlapi.model.OWLOntology
//
//class MainSimple {
//
//  val owlFile = ""
//  val r2rmlFile = ""
//  val propertyFile = ""
//
//  val configuration: OntopSQLOWLAPIConfiguration = OntopSQLOWLAPIConfiguration.defaultBuilder()
//    .ontologyFile(owlFile)
//    .r2rmlMappingFile(r2rmlFile)
//    .propertyFile(propertyFile)
//    .enableTestMode()
//    .build()
//
//  //  CHECK
//  val ontology = new OWLOntology() {}
//  val builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
//  builder.ontology(ontology)
//
//  val repo: Repository = OntopRepository.defaultRepository(configuration);
//  repo.initialize()
//
//}