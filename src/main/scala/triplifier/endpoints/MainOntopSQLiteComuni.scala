package triplifier.endpoints

import org.openrdf.rio.RDFFormat
import java.io.FileOutputStream
import java.io.File
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import triplifier.processors.OntopProcessor

object MainOntopSQLiteComuni extends App {

  val dump_file = "target/EXPORT/DUMP_comuni.nt"

  val ontop = OntopProcessor.sqlite

  val mappings = List(
    R2RMLComuni.comuni_comuni,
    R2RMLComuni.comuni_province,
    R2RMLComuni.comuni_regioni)

  val r2rml_model = ontop.loadR2RMLString(mappings.mkString("\n"))

  val fos = new FileOutputStream(new File(dump_file))
  ontop.dump(mappings, fos, RDFFormat.TURTLE)
  fos.flush()
  fos.close()

  // TODO: MOCK SPARQL in.memory

  val preview = ontop.previewDump(dump_file)
  println(preview, 100, 200)

}

private object R2RMLComuni {

  def comuni_regioni = """

    @prefix rr: <http://www.w3.org/ns/r2rml#> .
    @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
    @prefix l0: <https://w3id.org/italia/onto/l0/> .
    @prefix clvapit: <https://w3id.org/italia/onto/CLV/> .
    
    @base  <https://w3id.org/italia/> .
    
    
    <VIEW_regioni> rr:sqlQuery "
      
      SELECT 
        codice_regione AS _CODICE_REGIONE 
        ,denominazione_regione AS _NOME_REGIONE 
        ,codice_nuts2_2006 AS _NUTS2  
      FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani' 
    
    "
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
    	  rr:predicate clvapit:situatedWithin ; 
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

  def comuni_province = """

    @prefix rr: <http://www.w3.org/ns/r2rml#> .
    @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
    @prefix l0: <https://w3id.org/italia/onto/l0/> .
    @prefix clvapit: <https://w3id.org/italia/onto/CLV/> .
    @prefix nuts: <http://nuts.geovocab.org/id/> .
    
    @base  <https://w3id.org/italia/> .
    
    
    <VIEW_province> rr:sqlQuery "
      
      SELECT 
        codice_provincia AS _CODICE_PROVINCIA   
        ,codice_regione AS _CODICE_REGIONE 
        ,denominazione_regione AS _NOME_REGIONE 
        ,denominazione_provincia AS _NOME_PROVINCIA 
        ,sigla_automobilistica AS _SIGLA_AUTOMOBILISTICA
        ,codice_nuts2_2006 AS _NUTS2
        ,codice_nuts3_2006 AS _NUTS3  
      FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani'  
    
    "
    .
    
    <TriplesMap_SiglaAutomobilistica> a rr:TriplesMapClass ;
    
      rr:logicalTable <VIEW_province> ;
      
      rr:subjectMap [ 
    	  rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/sigla-automobilistica-{'_SIGLA_AUTOMOBILISTICA'}" ;
    		rr:class clvapit:Identifier ;
    	] ;
    	rr:predicateObjectMap [ 
        rr:predicate l0:identifier ; 
        rr:objectMap [ rr:column "_SIGLA_AUTOMOBILISTICA" ; ]
      ] ;
      rr:predicateObjectMap [ 
        rr:predicate clvapit:identifierType ; 
        rr:objectMap [ rr:constant "Sigla Automobilistica" ; ]
      ] ;
      
    .
    
    <TriplesMap_Province> a rr:TriplesMapClass ;
    
    	rr:logicalTable <VIEW_province> ;
    	    	
    	rr:subjectMap [ 
    		rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/provinces/{'_CODICE_PROVINCIA'}" ;
    		rr:class skos:Concept, clvapit:Province ;
    	] ;
    	
    	rr:predicateObjectMap [ 
        rr:predicate l0:name ; 
        rr:objectMap [ rr:column "_NOME_PROVINCIA" ; rr:language "it" ]
      ] ;
    	
      rr:predicateObjectMap [ 
    	  rr:predicate clvapit:situatedWithin ; 
    	  rr:objectMap [ 
    	  	rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/regions/{'_CODICE_REGIONE'}" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	
    	rr:predicateObjectMap [ 
    	  rr:predicate clvapit:hasIdentifier ; 
    	  rr:objectMap [ 
    	  	rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/sigla-automobilistica-{'_SIGLA_AUTOMOBILISTICA'}" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	
    	rr:predicateObjectMap [ 
    	  rr:predicate owl:sameAs ; 
    	  rr:objectMap [ 
    	  	rr:template "http://nuts.geovocab.org/id/{'_NUTS3'}" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	
     .
     
  """

  def comuni_comuni = """

    @prefix rr: <http://www.w3.org/ns/r2rml#> .
    @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
    @prefix l0: <https://w3id.org/italia/onto/l0/> .
    @prefix clvapit: <https://w3id.org/italia/onto/CLV/> .
    @prefix tiapit: <https://w3id.org/italia/onto/TI/> .
    
    @base  <https://w3id.org/italia/> .
    
    <VIEW_comuni> rr:sqlQuery "
      
      SELECT DISTINCT
      ISTAT.denominazione_corrente AS _NOME   
      ,ANPR.stato AS _STATO 
      ,ANPR.codcatastale AS _CODICE_CATASTALE 
      ,ISTAT.codice_provincia AS _CODICE_PROVINCIA 
      ,ISTAT.codice_comune_formato_numerico AS _CODICE_ISTAT
      ,dataistituzione AS _DATA_ISTITUZIONE
      ,datacessazione AS _DATA_CESSAZIONE
      ,dataultimoagg AS _DATA_AGGIORNAMENTO 
      FROM 'gove__amministrazione.default_org_o_anpr_archivio_storico_comuni' AS ANPR 
      JOIN 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani' AS ISTAT 
      ON (ISTAT.codice_comune_formato_numerico=ANPR.codistat)
      ORDER BY _NOME 
    
    "
    .
    
    # TODO: add UDF for time / date
    
    <TriplesMap_TimeInterval> a rr:TriplesMapClass ;
    
      rr:logicalTable <VIEW_comuni> ;
      
      rr:subjectMap [ 
    		rr:template "https://w3id.org/italia/controlled-vocabulary/time-intervals/({'_DATA_ISTITUZIONE'}-{'_DATA_CESSAZIONE'})" ;
    		rr:class tiapit:TimeInterval ;
    	] ;
    	rr:predicateObjectMap [ 
        rr:predicate clvapit:start_date ; 
        rr:objectMap [ rr:column "_DATA_ISTITUZIONE" ; ]
      ] ;
      rr:predicateObjectMap [ 
        rr:predicate clvapit:end_date ; 
        rr:objectMap [ rr:column "_DATA_CESSAZIONE" ; ]
      ] ;
      rr:predicateObjectMap [ 
        rr:predicate clvapit:date ; 
        rr:objectMap [ rr:column "_DATA_AGGIORNAMENTO" ; ]
      ] ;
      
    .
    
    <TriplesMap_CodiceISTAT> a rr:TriplesMapClass ;
    
      rr:logicalTable <VIEW_comuni> ;
      
      rr:subjectMap [ 
    		rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/codice-istat-{'_CODICE_ISTAT'}" ;
    		rr:class clvapit:Identifier ;
    	] ;
    	rr:predicateObjectMap [ 
        rr:predicate l0:identifier ; 
        rr:objectMap [ rr:column "_CODICE_ISTAT" ; ]
      ] ;
      rr:predicateObjectMap [ 
        rr:predicate clvapit:identifierType ; 
        rr:objectMap [ rr:constant "Codice ISTAT" ; ]
      ] ;
      
    .
    
    <TriplesMap_CodiceCatastale> a rr:TriplesMapClass ;
    
      rr:logicalTable <VIEW_comuni> ;
      
      rr:subjectMap [ 
    		rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/codice-catastale-{'_CODICE_CATASTALE'}" ;
    		rr:class clvapit:Identifier ;
    	] ;
    	rr:predicateObjectMap [ 
        rr:predicate l0:identifier ; 
        rr:objectMap [ rr:column "_CODICE_CATASTALE" ; ]
      ] ;
      rr:predicateObjectMap [ 
        rr:predicate clvapit:identifierType ; 
        rr:objectMap [ rr:constant "Codice Catastale" ; ]
      ] ;
      
    .
    
    <TriplesMap_Comuni> a rr:TriplesMapClass ;
    
    	rr:logicalTable <VIEW_comuni> ;
    	
    	# rr:logicalTable [ rr:tableName "TEST_COMUNI" ]; // HACK locale per le date
    	    	
    	rr:subjectMap [ 
    		rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/cities/{'_CODICE_ISTAT'}@{'_DATA_ISTITUZIONE'}" ;
    		rr:class skos:Concept, clvapit:City ;
    	] ;
    	
      rr:predicateObjectMap [ 
    	  rr:predicate clvapit:situatedWithin ; 
    	  rr:objectMap [ 
    	  	rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/provinces/{'_CODICE_PROVINCIA'}" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	
    	rr:predicateObjectMap [ 
    	  rr:predicate clvapit:hasIdentifier ; 
    	  rr:objectMap [ 
    	  	rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/codice-istat-{'_CODICE_ISTAT'}" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	rr:predicateObjectMap [ 
    	  rr:predicate clvapit:hasIdentifier ; 
    	  rr:objectMap [ 
    	  	rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/codice-catastale-{'_CODICE_CATASTALE'}" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	
    	rr:predicateObjectMap [ 
        rr:predicate l0:name ; 
        rr:objectMap [ rr:column "_NOME" ; rr:language "it" ]
      ] ;
      
      rr:predicateObjectMap [ 
        rr:predicate l0:_STATO ; 
        rr:objectMap [ rr:column "_STATO" ; ] # CHECK: da capire dove mappare questa informazione!
      ] ;
    	
    	rr:predicateObjectMap [ 
    	  rr:predicate clvapit:has_validity ; 
    	  rr:objectMap [ 
    		  rr:template "https://w3id.org/italia/controlled-vocabulary/time-intervals/({'_DATA_ISTITUZIONE'}-{'_DATA_CESSAZIONE'})" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	
      rr:predicateObjectMap [ 
        rr:predicate clvapit:time_of_issuance ; 
        rr:objectMap [ rr:column "_DATA_ISTITUZIONE" ; ] # HACK: va aggiunto rr:datatype xsd:date (valutare UDF su sqlite)
      ] ;
      rr:predicateObjectMap [ 
        rr:predicate clvapit:date ; # DATA di AGGIORNAMENTO? 
        rr:objectMap [ rr:column "_DATA_CESSAZIONE" ;  ]
      ] ;
      rr:predicateObjectMap [ 
        rr:predicate clvapit:update_time ; 
        rr:objectMap [ rr:column "_DATA_AGGIORNAMENTO" ; ]
      ] ;
      
     .
     
  """

}