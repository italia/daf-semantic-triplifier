package triplifier.examples

import org.openrdf.rio.RDFFormat
import java.io.FileOutputStream
import java.io.File
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import triplifier.processors.OntopProcessor

object MainOntopSQLiteComuni extends App {

  val dump_file_name = "target/EXPORT/DUMP_comuni.nt"
  val dump_file = new File(dump_file_name).getAbsoluteFile
  if (!dump_file.getParentFile.exists()) dump_file.getParentFile.mkdirs()

  val ontop = OntopProcessor.sqlite

  val mappings = List(
    R2RMLComuniSQLite.citta_metropolitane,
    R2RMLComuniSQLite.regioni,
    R2RMLComuniSQLite.province,
    R2RMLComuniSQLite.comuni)

  val r2rml_model = ontop.loadTurtle(mappings.mkString("\n"))

  val fos = new FileOutputStream(dump_file)
  ontop.dump(mappings)(None)(None)(fos, RDFFormat.TURTLE)
  fos.flush()
  fos.close()

  // TODO: MOCK SPARQL in.memory

  val preview = ontop.previewDump(dump_file_name)
  println(preview, 100, 200)

}

object R2RMLComuniSQLite {

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
      FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani' 
    
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

  def citta_metropolitane = """

    @prefix rr: <http://www.w3.org/ns/r2rml#> .
    @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
    @prefix l0: <https://w3id.org/italia/onto/l0/> .
    @prefix clvapit: <https://w3id.org/italia/onto/CLV/> .
    @prefix nuts: <http://nuts.geovocab.org/id/> .
    
    @base  <https://w3id.org/italia/> .
    
    # CITTA METROPOLITANE 
    
    <VIEW_citta_metropolitane> rr:sqlQuery "
      
      SELECT 
      	codice_provincia AS _CODICE_PROVINCIA 
      	,codice_citta_metropolitana AS _CODICE_CITTA_METROPOLITANA  
      	,codice_regione AS _CODICE_REGIONE 
      	,denominazione_regione AS _NOME_REGIONE 
      	,denominazione_provincia AS _NOME_PROVINCIA 
      	,denominazione_citta_metropolitana AS _NOME_CITTA_METROPOLITANA 
      	,sigla_automobilistica AS _SIGLA_AUTOMOBILISTICA
      	,codice_nuts2_2006 AS _NUTS2
      	,codice_nuts3_2006 AS _NUTS3
      	,*
      FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani'
      WHERE (_NOME_PROVINCIA = '-' AND _NOME_CITTA_METROPOLITANA != '-') 
    
    "
    .
    
    <TriplesMap_CittaMetropolitana> a rr:TriplesMapClass ;
    
      rr:logicalTable <VIEW_citta_metropolitane> ;
      
      rr:subjectMap [ 
    	  rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/codice-citta-metropolitana/{'_CODICE_CITTA_METROPOLITANA'}" ;
    		rr:class clvapit:Identifier ;
    	] ;
    	rr:predicateObjectMap [ 
        rr:predicate l0:identifier ; 
        rr:objectMap [ rr:column "_CODICE_CITTA_METROPOLITANA" ; ]
      ] ;
      rr:predicateObjectMap [ 
        rr:predicate clvapit:identifierType ; 
        rr:objectMap [ rr:constant "Codice Città Metropolitana" ; ]
      ] ;
      
    .
    
    <TriplesMap_CittaMetropolitane> a rr:TriplesMapClass ;
    
      rr:logicalTable <VIEW_citta_metropolitane> ;
    	    	
    	rr:subjectMap [ 
    		rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/provinces/{'_CODICE_PROVINCIA'}" ;
    		rr:class skos:Concept, clvapit:Province ;
    	] ;
    	
    	rr:predicateObjectMap [ 
    	  rr:predicate clvapit:hasIdentifier ; 
    	  rr:objectMap [ 
    	    rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/codice-citta-metropolitana/{'_CODICE_CITTA_METROPOLITANA'}" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	
    	rr:predicateObjectMap [ 
        rr:predicate l0:name ; 
        rr:objectMap [ rr:column "_NOME_CITTA_METROPOLITANA" ; rr:language "it" ]
      ] ;
      
      rr:predicateObjectMap [ 
        rr:predicate clvapit:acronym ; 
        rr:objectMap [ rr:column "_SIGLA_AUTOMOBILISTICA" ; ]
      ] ;
      
      rr:predicateObjectMap [ 
    	  rr:predicate clvapit:hasRank ; 
    	  rr:objectMap [ rr:constant 3 ; rr:datatype xsd:integer ] 
    	] ;
    	
      rr:predicateObjectMap [ 
    	  rr:predicate clvapit:situatedWithin, clvapit:hasDirectHigherRank ; 
    	  rr:objectMap [ 
    	  	rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/regions/{'_CODICE_REGIONE'}" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	
    	rr:predicateObjectMap [ 
    	  rr:predicate clvapit:hasIdentifier ; 
    	  rr:objectMap [ 
    	  	rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/sigla-automobilistica/{'_SIGLA_AUTOMOBILISTICA'}" ;
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

  def province = """

    @prefix rr: <http://www.w3.org/ns/r2rml#> .
    @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
    @prefix l0: <https://w3id.org/italia/onto/l0/> .
    @prefix clvapit: <https://w3id.org/italia/onto/CLV/> .
    @prefix nuts: <http://nuts.geovocab.org/id/> .
    
    @base  <https://w3id.org/italia/> .    
    
    # PROVINCE 
    
    <VIEW_province> rr:sqlQuery "
      
      SELECT 
      	codice_provincia AS _CODICE_PROVINCIA 
      	,codice_citta_metropolitana AS _CODICE_CITTA_METROPOLITANA 
      	,codice_regione AS _CODICE_REGIONE 
      	,denominazione_regione AS _NOME_REGIONE 
      	,denominazione_provincia AS _NOME_PROVINCIA 
      	,denominazione_citta_metropolitana AS _NOME_CITTA_METROPOLITANA 
      	,sigla_automobilistica AS _SIGLA_AUTOMOBILISTICA
      	,codice_nuts2_2006 AS _NUTS2
      	,codice_nuts3_2006 AS _NUTS3 
      FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani'
      WHERE (_NOME_PROVINCIA != '-' AND _NOME_CITTA_METROPOLITANA = '-')  
    
    "
    .
    
    <TriplesMap_SiglaAutomobilistica> a rr:TriplesMapClass ;
    
      rr:logicalTable <VIEW_province> ;
      
      rr:subjectMap [ 
    	  rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/sigla-automobilistica/{'_SIGLA_AUTOMOBILISTICA'}" ;
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
        rr:predicate clvapit:acronym ; 
        rr:objectMap [ rr:column "_SIGLA_AUTOMOBILISTICA" ; ]
      ] ;
      
      rr:predicateObjectMap [ 
    	  rr:predicate clvapit:hasRank ; 
    	  rr:objectMap [ rr:constant 3 ; rr:datatype xsd:integer ] 
    	] ;
    	
      rr:predicateObjectMap [ 
    	  rr:predicate clvapit:situatedWithin, clvapit:hasDirectHigherRank ;
    	  rr:objectMap [ 
    	  	rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/regions/{'_CODICE_REGIONE'}" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	
    	rr:predicateObjectMap [ 
    	  rr:predicate clvapit:hasIdentifier ; 
    	  rr:objectMap [ 
    	  	rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/sigla-automobilistica/{'_SIGLA_AUTOMOBILISTICA'}" ;
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

  def comuni = """

    @prefix rr: <http://www.w3.org/ns/r2rml#> .
    @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
    @prefix l0: <https://w3id.org/italia/onto/l0/> .
    @prefix clvapit: <https://w3id.org/italia/onto/CLV/> .
    @prefix tiapit: <https://w3id.org/italia/onto/TI/> .
    
    @base  <https://w3id.org/italia/> .
    
    # COMUNI
    
    <VIEW_comuni_view_sqlite> rr:sqlQuery "
    
      SELECT DISTINCT
      	denominazione_corrente AS _NOME   
      	,stato AS _STATO 
      	,codcatastale AS _CODICE_CATASTALE 
      	,codice_provincia AS _CODICE_PROVINCIA 
      	,codice_comune_formato_numerico AS _CODICE_ISTAT
      	,dataistituzione AS _DATA_ISTITUZIONE
      	,datacessazione AS _DATA_CESSAZIONE
      	,dataultimoagg AS _DATA_AGGIORNAMENTO 
      FROM TEST_COMUNI 
      ORDER BY _NOME
    
    "
    .
    
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
    
      rr:logicalTable [ rr:tableName "TEST_COMUNI" ]; 
      
      rr:subjectMap [ 
    		rr:template "https://w3id.org/italia/controlled-vocabulary/time-intervals/({'_DATA_ISTITUZIONE'})-({'_DATA_CESSAZIONE'})" ;
    		rr:class tiapit:TimeInterval ;
    	] ;
    	rr:predicateObjectMap [ 
        rr:predicate tiapit:startTime ; 
        rr:objectMap [ rr:column "_DATA_ISTITUZIONE" ; ]
      ] ;
      rr:predicateObjectMap [ 
        rr:predicate tiapit:endTime ; 
        rr:objectMap [ rr:column "_DATA_CESSAZIONE" ; ]
      ] ;
      
    .    
    
    <TriplesMap_CodiceISTAT> a rr:TriplesMapClass ;
    
      rr:logicalTable [ rr:tableName "TEST_COMUNI" ]; 
      
      rr:subjectMap [ 
    		rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/codice-istat/{'_CODICE_ISTAT'}" ;
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
    
    
    <TriplesMap_CodiceProgressivoComune> a rr:TriplesMapClass ;
    
    	rr:logicalTable [ rr:tableName "TEST_COMUNI" ];
      
      rr:subjectMap [ 
    		rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/progressivo-comune/{'_CODICE_PROGRESSIVO'}" ;
    		rr:class clvapit:Identifier ;
    	] ;
    	rr:predicateObjectMap [ 
        rr:predicate l0:identifier ; 
        rr:objectMap [ rr:column "_CODICE_PROGRESSIVO" ; ]
      ] ;
      rr:predicateObjectMap [ 
        rr:predicate clvapit:identifierType ; 
        rr:objectMap [ rr:constant "Progressivo del comune" ; ]
      ] ;
      
    .
    
    <TriplesMap_CodiceCatastale> a rr:TriplesMapClass ;
    
      rr:logicalTable [ rr:tableName "TEST_COMUNI" ];
      
      rr:subjectMap [ 
    		rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/codice-catastale/{'_CODICE_CATASTALE'}" ;
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
    
      rr:logicalTable [ rr:tableName "TEST_COMUNI" ]; # HACK locale per le date
    	    	
    	rr:subjectMap [ 
    		rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/cities/{'_CODICE_ISTAT'}-({'_DATA_ISTITUZIONE'})" ;
    		rr:class skos:Concept, clvapit:City ;
    	] ;
    	
      rr:predicateObjectMap [ 
    	  rr:predicate clvapit:situatedWithin, clvapit:hasDirectHigherRank ; 
    	  rr:objectMap [ 
    	  	rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/provinces/{'_CODICE_PROVINCIA'}" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	
    	rr:predicateObjectMap [ 
    	  rr:predicate clvapit:hasRank ; 
    	  rr:objectMap [ rr:constant 4 ; rr:datatype xsd:integer ] 
    	] ;
    	
    	rr:predicateObjectMap [ 
    	  rr:predicate clvapit:hasIdentifier ; 
    	  rr:objectMap [ 
    	  	rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/progressivo-comune/{'_CODICE_PROGRESSIVO'}" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	rr:predicateObjectMap [ 
    	  rr:predicate clvapit:hasIdentifier ; 
    	  rr:objectMap [ 
    	  	rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/codice-istat/{'_CODICE_ISTAT'}" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	rr:predicateObjectMap [ 
    	  rr:predicate clvapit:hasIdentifier ; 
    	  rr:objectMap [ 
    	  	rr:template "https://w3id.org/italia/controlled-vocabulary/identifiers/codice-catastale/{'_CODICE_CATASTALE'}" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	
    	rr:predicateObjectMap [ 
        rr:predicate l0:name ; 
        rr:objectMap [ rr:column "_NOME" ; rr:language "it" ]
      ] ;
      
    	rr:predicateObjectMap [ 
    	  rr:predicate clvapit:hasSOValidity ; 
    	  rr:objectMap [ 
    		  rr:template "https://w3id.org/italia/controlled-vocabulary/time-intervals/({'_DATA_ISTITUZIONE'})-({'_DATA_CESSAZIONE'})" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	
      rr:predicateObjectMap [ 
        rr:predicate tiapit:modified ; 
        rr:objectMap [ rr:column "_DATA_AGGIORNAMENTO" ; ]
      ] ;
      
     .
     
  """

}