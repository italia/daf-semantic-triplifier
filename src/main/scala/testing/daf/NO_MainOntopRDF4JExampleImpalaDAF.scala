package testing.daf;
//package other
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
//object MainOntopRDF4JExampleImpalaDAF extends App {
//
//  val jdbc_driver = "com.cloudera.impala.jdbc41.Driver"
//  val jdbc_dsn = "jdbc:impala://slave4.platform.daf.gov.it:21050;SSL=1;SSLKeyStore=C:/Users/Al.Serafini/awavedev/progetti/DAF/ssl_impala/master-impala.jks;SSLKeyStorePwd=Ahdai5th;AuthMech=3;CAIssuedCertNamesMismatch=1"
//  val jdbc_name = "daf.impala"
//  val jdbc_user = "aserafini"
//  val jdbc_password = "openD4ti"
//
//  val r2rmlFile = Paths.get("src/test/resources/r2rml/poc_anpr_comuni.r2rml.ttl").toUri().toString()
//
//  val configuration: OntopSQLOWLAPIConfiguration = OntopSQLOWLAPIConfiguration.defaultBuilder()
//    // .ontologyFile(owlFile)
//    .r2rmlMappingFile(r2rmlFile)
//    // .propertyFile(propertyFile)
//    .jdbcDriver(jdbc_driver)
//    .jdbcUrl(jdbc_dsn)
//    .jdbcName(jdbc_name)
//    .jdbcUser(jdbc_user)
//    .jdbcPassword(jdbc_password)
////    .enableTestMode()
////    .enableProvidedDBMetadataCompletion(true)
////    .enableExistentialReasoning(true)
////    .enableDefaultDatatypeInference(true)
////    .enableFullMetadataExtraction(true)
////    .enableOntologyAnnotationQuerying(true)
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