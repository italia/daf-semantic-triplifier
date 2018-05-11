package testing.daf;
//package examples
//
//import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
//import org.eclipse.rdf4j.model.Statement;
//import org.eclipse.rdf4j.query._
//import org.eclipse.rdf4j.repository.Repository;
//import org.eclipse.rdf4j.repository.RepositoryConnection;
//
//import java.nio.file.Files;
//import java.nio.file.Paths;
//
//import java.util.stream.Collectors.joining
//import it.unibz.inf.ontop.injection.OntopSystemConfiguration
//import it.unibz.inf.ontop.answering.OntopQueryEngine
//import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration
//import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration
//import java.io.File
//
//object MainOntopRDF4JExample extends App {
//
//  val db_path = Paths.get("C:/Users/Al.Serafini/repos/DAF/db/test_comuni.db").normalize()
//
//  val jdbc_driver = "org.sqlite.JDBC"
//  val jdbc_dsn = s"jdbc:sqlite:${db_path}"
//  val jdbc_name = "daf.sqlite"
//  val jdbc_user = "aserafini"
//  val jdbc_password = "openD4ti"
//
//  val r2rmlFile = Paths.get("src/test/resources/r2rml/poc_anpr_comuni.r2rml.ttl").toUri().toString()
//
//  val configuration: OntopSQLOWLAPIConfiguration = OntopSQLOWLAPIConfiguration.defaultBuilder()
//    .r2rmlMappingFile(r2rmlFile)
//    .jdbcDriver(jdbc_driver)
//    .jdbcUrl(jdbc_dsn)
////    .jdbcName(jdbc_name)
//    .jdbcUser(jdbc_user)
//    .jdbcPassword(jdbc_password)
//    .enableTestMode()
//    .build()
//
//  println("#### START")
//
//  val repo: Repository = OntopRepository.defaultRepository(configuration);
//  repo.initialize();
//
//  val sparqlQuery = "SELECT (COUNT(*) AS ?triples) WHERE { ?s ?p ?o } LIMIT 100"
//
//  val conn1: RepositoryConnection = repo.getConnection()
//  val result1: TupleQueryResult = conn1.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery).evaluate()
//  while (result1.hasNext()) {
//    val bindingSet: BindingSet = result1.next()
//    println(bindingSet)
//  }
//
//  repo.shutDown()
//
//  println("#### STOP")
//
//}