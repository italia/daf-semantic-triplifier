//package daf.testing.triplification
//
//import it.unibz.inf.ontop.sesame.SesameVirtualRepo
//import org.slf4j.LoggerFactory
//import java.sql.DriverManager
//import java.io.File
//import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences
//import java.time.LocalDateTime
//import org.openrdf.repository.RepositoryConnection
//
//import scala.collection.JavaConversions._
//import scala.collection.JavaConverters._
//import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants
//import java.io.FileOutputStream
//import org.openrdf.rio.RDFFormat
//import org.openrdf.rio.Rio
//import scala.concurrent.duration.Duration
//import java.util.concurrent.TimeUnit
//import java.nio.file.Files
//import scala.util.Random
//import org.openrdf.model.Statement
//import org.semanticweb.owlapi.model.OWLOntology
//import org.semanticweb.owlapi.apibinding.OWLManager
//import org.openrdf.model.impl.LinkedHashModel
//import org.openrdf.model.Model
//import org.openrdf.rio.RDFParser
//import org.openrdf.rio.helpers.StatementCollector
//import java.io.FileInputStream
//import scala.io.Source
//import java.io.ByteArrayInputStream
//import java.io.OutputStream
//import java.nio.file.Paths
//import org.openrdf.model.ValueFactory
//import com.typesafe.config.Config
//import com.typesafe.config.ConfigFactory
//import info.aduna.iteration.Iterations
//import daf.triplification.OntopProcessor
//
//// THIS IS A WORKING EXAMPLE, for DAF-IMPALA + ONTOP
//object Example extends App {
//
//  val dump = "target/EXPORT/testing_rdf.nt"
//
//  val ontop = new OntopProcessor()
//
//  println("#### RDF mapping - START")
//
//  ontop.writeToFile(dump)
//
//  val preview = ontop.previewDump(dump)
//
//  println("\n\n\n\nRDF DUMP")
//  println(preview)
//
//  println("#### RDF mapping - STOP")
//  System.exit(0)
//
//}
//
//object R2RMLExamples {
//
//  def test01 = """
//  
//  @prefix rr: <http://www.w3.org/ns/r2rml#> .
//  @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
//  
//  @prefix l0: <http://w3id.org/italia/onto/l0/> .
//  @prefix clvapit: <http://w3id.org/italia/onto/CLV/> .
//  
//  @base  <https://w3id.org/italia/> .
//  
//  <Map_Regione> a rr:TriplesMapClass ;
//  		
//  	rr:logicalTable [ rr:tableName "gove__amministrazione.default_org_o_anpr_archivio_storico_comuni" ];
//  	
//  	rr:subjectMap [ 
//  		rr:template "http://w3id.org/italia/territorial-classifications/regions/{'idregione'}" ;
//  		rr:class skos:Concept, clvapit:Region, clvapit:Feature, clvapit:AddressComponent, clvapit:NamedAddressComponent ;
//  	] ;
//  	
//  	rr:predicateObjectMap [ 
//  	  rr:predicate l0:name ; 
//  	  rr:objectMap [ rr:column "idregione" ; rr:language "it" ]
//  	] ;
//  
//  	rr:predicateObjectMap [ 
//  	  rr:predicate clvapit:situatedWithin ; 
//  	  rr:objectMap [ 
//  	  	rr:template "http://w3id.org/italia/territorial-classifications/countries/ITA" ;
//  	  	rr:termType rr:IRI ;
//  	  ] 
//  	] ;
//  	
//  	rr:predicateObjectMap [ 
//  	  rr:predicate clvapit:hasIdentifier ; 
//  	  rr:objectMap [
//  	  	rr:template "http://w3id.org/italia/identifiers/cr-{'idregione'}" ;
//  	  	rr:termType rr:IRI ;
//  	  ] 
//  	] ;
//  		
//   .
//
//  <Map_CodiceRegione> a rr:TriplesMapClass ;
//  	
//  	rr:logicalTable [ rr:tableName "gove__amministrazione.default_org_o_anpr_archivio_storico_comuni" ];
//  	
//  	rr:subjectMap [ 
//  		rr:template "http://w3id.org/italia/identifiers/cr-{'idregione'}" ;
//  		rr:class clvapit:Identifier ;
//  	] ;
//  	
//  	rr:predicateObjectMap [ 
//  	  rr:predicate l0:identifier ; 
//  	  rr:objectMap [ rr:column "idregione" ]
//  	] ;
//  
//  	rr:predicateObjectMap [ 
//  	  rr:predicate clvapit:identifierType ; 
//  	  rr:objectMap [ rr:constant "Codice Regione" ] 
//  	] ;
//  	
//   .
//    
//  """
//
//  def anpr_comuni = """
//    
//    @prefix rr: <http://www.w3.org/ns/r2rml#>.
//    @prefix ex: <http://example.com/ns#>.
//    
//    <TriplesMap1> a rr:TriplesMapClass;
//
//    rr:logicalTable [ rr:tableName "gove__amministrazione.default_org_o_anpr_archivio_storico_comuni" ];
//    
//    rr:subjectMap [
//    	rr:template "http://w3id.org/italia/resources/comuni/{id}" ;
//    	rr:class ex:City ;
//    ] ;
//    
//    rr:predicateObjectMap [ 
//      rr:predicate ex:name; 
//      rr:objectMap [
//      	rr:column "denominazione_it" ;
//      ]
//    ] ;
//
//    .
//
//  """
//
//}