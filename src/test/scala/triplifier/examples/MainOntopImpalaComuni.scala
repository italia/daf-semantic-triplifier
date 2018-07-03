package triplifier.examples

import org.openrdf.rio.RDFFormat
import java.io.FileOutputStream
import java.io.File
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import triplifier.processors.OntopProcessor

object MainOntopImpalaComuni extends App {

  val dump_file_name = "target/EXPORT/DUMP_comuni.nt"
  val dump_file = new File(dump_file_name).getAbsoluteFile
  if (!dump_file.getParentFile.exists()) dump_file.getParentFile.mkdirs()

  val ontop = OntopProcessor.impala

  val mappings = List(R2RMLComuniImpala.regioni)

  val r2rml_model = ontop.loadTurtle(mappings.mkString("\n"))

  val fos = new FileOutputStream(dump_file)
  ontop.dump(mappings)(None)(None)(fos, RDFFormat.TURTLE)
  fos.flush()
  fos.close()

  // TODO: MOCK SPARQL in.memory

  val preview = ontop.previewDump(dump_file_name)
  println(preview, 100, 200)

}

object R2RMLComuniImpala {

  def regioni = """

      @prefix rr: <http://www.w3.org/ns/r2rml#> .
      @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
      @prefix l0: <https://w3id.org/italia/onto/l0/> .
      @prefix clvapit: <https://w3id.org/italia/onto/CLV/> .
      
      @base  <https://w3id.org/italia/> .
      
      # REGIONI 
      
      <VIEW_regioni> rr:sqlQuery "
        
        SELECT 
          codice_regione AS _CODICE_REGIONE 
          ,denominazione_regione AS _NOME_REGIONE 
          ,codice_nuts2_2006 AS _NUTS2  
        FROM gove__amministrazione.default_org_o_istat_elenco_comuni_italiani 
      
      "
      .
      
      <TriplesMap_CodiceRegione> a rr:TriplesMapClass ;
      
        rr:logicalTable <VIEW_regioni> ;
        
        rr:subjectMap [ 
      	  rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/cr-{'_CODICE_REGIONE'}" ;
      		rr:class clvapit:Identifier ;
      	] ;
      	rr:predicateObjectMap [ 
          rr:predicate l0:identifier ; 
          rr:objectMap [ rr:column "_CODICE_REGIONE" ; ]
        ] ;
        rr:predicateObjectMap [ 
          rr:predicate clvapit:identifierType ; 
          rr:objectMap [ rr:constant "Codice Regione" ; ]
        ] ;
        
      .
      
      <TriplesMap_Region> a rr:TriplesMapClass ;
      
      	rr:logicalTable <VIEW_regioni> ;
      	    	
      	rr:subjectMap [ 
      		rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/regions/{'_CODICE_REGIONE'}" ;
      		rr:class skos:Concept, clvapit:Region ;
      	] ;
      	
      	rr:predicateObjectMap [ 
          rr:predicate l0:name ; 
          rr:objectMap [ rr:column "_NOME_REGIONE" ; rr:language "it" ]
        ] ;
        
        rr:predicateObjectMap [ 
      	  rr:predicate clvapit:hasRank ; 
      	  rr:objectMap [ rr:constant 2 ; rr:datatype xsd:integer ] 
      	] ;
        
        rr:predicateObjectMap [ 
      	  rr:predicate clvapit:hasIdentifier ; 
      	  rr:objectMap [ 
      	  	rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/cr-{'_CODICE_REGIONE'}" ;
      	  	rr:termType rr:IRI ;
      	  ] 
      	] ;
      	
      	rr:predicateObjectMap [ 
      	  rr:predicate clvapit:situatedWithin, clvapit:hasDirectHigherRank ; 
      	  rr:objectMap [ 
      	  	rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/countries/ITA" ;
      	  	rr:termType rr:IRI ;
      	  ] 
      	] ;
      	
      	rr:predicateObjectMap [ 
      	  rr:predicate owl:sameAs ; 
      	  rr:objectMap [ 
      	  	rr:template "http://nuts.geovocab.org/id/{'_NUTS2'}" ;
      	  	rr:termType rr:IRI ;
      	  ] 
      	] ;
      	
       .
       
    """

}