package daf.poc.rdf

import org.openrdf.rio.RDFFormat
import triplifier.processors.OntopProcessor

object MainImpalaStatiEsteri extends App {

  val ontop = OntopProcessor.impala

  def r2rml = R2RMLStatiEsteri.stati_esteri

  ontop.dump(List(r2rml))(None)(None)(System.out, RDFFormat.TURTLE)

}

object R2RMLStatiEsteri {

  def stati_esteri = """

    @prefix rr: <http://www.w3.org/ns/r2rml#> .
    @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
    @prefix l0: <https://w3id.org/italia/onto/l0/> .
    @prefix clvapit: <https://w3id.org/italia/onto/CLV/> .
    
    @base  <https://w3id.org/italia/> .
    
    
    <VIEW_regioni> rr:sqlQuery "
      
      SELECT 
      id 
      , denominazione AS _DENOMINAZIONE 
      , denominazioneistat AS _DENOMINAZIONE_ISTAT_IT 
      , denominazioneistat_en AS _DENOMINAZIONE_ISTAT_EN 
      , datainiziovalidita
      , datafinevalidita
      , codiso3166_1_alpha3 AS _CODICE_ISO 
      , codmae
      , codmin
      , codat
      , codistat
      , cittadinanza
      , nascita
      , residenza
      , fonte
      , tipo
      , codisosovrano
      , dataultimoagg
      , processing_dttm
      FROM gove__amministrazione.default_org_o_anpr_elenco_stati_esteri 
    
    "
    .
    
    <TriplesMap_Region> a rr:TriplesMapClass ;
    
    	rr:logicalTable <VIEW_regioni> ;
    	    	
    	rr:subjectMap [ 
    		rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/countries/{'_CODICE_ISO'}" ;
    		rr:class skos:Concept, clvapit:Country ;
    	] ;
    	
    	rr:predicateObjectMap [ 
        rr:predicate l0:name ; 
        rr:objectMap [ rr:column "_DENOMINAZIONE_ISTAT_IT" ; rr:language "it" ]
      ] ;
      rr:predicateObjectMap [ 
        rr:predicate l0:name ; 
        rr:objectMap [ rr:column "_DENOMINAZIONE_ISTAT_EN" ; rr:language "en" ]
      ] ;
    	
     .
     
  """

}