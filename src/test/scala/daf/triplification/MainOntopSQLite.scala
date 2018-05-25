package daf.triplification

import org.openrdf.rio.RDFFormat
import java.io.FileOutputStream
import java.io.File

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.openrdf.rio.Rio
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore

object MainOntopSQLite extends App {

  val dump_file = "target/EXPORT/DUMP.nt"

  val ontop = new OntopProcessor

  val mappings = List(
    R2RMLQueries.istat_comuni_00,
    R2RMLQueries.istat_comuni_01,
    R2RMLQueries.istat_comuni_02)

  val r2rml_model = ontop.loadR2RMLString(mappings.mkString("\n"))

  // CHECK namespaces
  val namespaces = r2rml_model.getNamespaces
  println("namespaces\n\t" + namespaces.mkString("\n\t"))

  val fos = new FileOutputStream(new File(dump_file))
  ontop.dump(mappings, fos, RDFFormat.NTRIPLES)
  fos.flush()
  fos.close()

  // TODO: MOCK SPARQL in.memory

  val preview = ontop.previewDump(dump_file)
  println(preview, 100, 200)

}

object R2RMLQueries {

  def istat_comuni_01 = """

    @prefix rr: <http://www.w3.org/ns/r2rml#> .
    @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
    @prefix l0: <https://w3id.org/italia/onto/l0/> .
    @prefix clvapit: <https://w3id.org/italia/onto/CLV/> .
    
    @base  <https://w3id.org/italia/> .
    
    
    <VIEW_regioni> rr:sqlQuery "
      
      SELECT codice_regione, *  
      FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani' 
    
    "
    .
    
    <TriplesMap_Region> a rr:TriplesMapClass ;
    
    	rr:logicalTable <VIEW_regioni> ;
    	    	
    	rr:subjectMap [ 
    		rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/regions/{'codice_regione'}" ;
    		rr:class skos:Concept, clvapit:Region ;
    	] ;
    	
    	rr:predicateObjectMap [ 
    	  rr:predicate clvapit:situatedWithin ; 
    	  rr:objectMap [ 
    	  	rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/countries/ITA" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	
     .
     
  """

  def istat_comuni_02 = """

    @prefix rr: <http://www.w3.org/ns/r2rml#> .
    @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
    @prefix l0: <https://w3id.org/italia/onto/l0/> .
    @prefix clvapit: <https://w3id.org/italia/onto/CLV/> .
    
    @base  <https://w3id.org/italia/> .
    
    
    <VIEW_province> rr:sqlQuery "
      
      SELECT 
        codice_provincia, 
        codice_regione, 
        *  
      FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani' 
    
    "
    .
    
    <TriplesMap_Province> a rr:TriplesMapClass ;
    
    	rr:logicalTable <VIEW_province> ;
    	    	
    	rr:subjectMap [ 
    		rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/provinces/{'codice_provincia'}" ;
    		rr:class skos:Concept, clvapit:Province ;
    	] ;
    	
      rr:predicateObjectMap [ 
    	  rr:predicate clvapit:situatedWithin ; 
    	  rr:objectMap [ 
    	  	rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/regions/{'codice_regione'}" ;
    	  	rr:termType rr:IRI ;
    	  ] 
    	] ;
    	
     .
     
  """

  def istat_comuni_00 = """

    @prefix rr: <http://www.w3.org/ns/r2rml#> .
    @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
    @prefix l0: <https://w3id.org/italia/onto/l0/> .
    @prefix clvapit: <https://w3id.org/italia/onto/CLV/> .
    
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
    
    <TriplesMap_Comuni> a rr:TriplesMapClass ;
    
    	rr:logicalTable <VIEW_comuni> ;
    	    	
    	rr:subjectMap [ 
    		rr:template "https://w3id.org/italia/controlled-vocabulary/territorial-classifications/cities/{'_DATA_ISTITUZIONE'}/{'_CODICE_ISTAT'}" ;
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
        rr:predicate l0:name ; 
        rr:objectMap [ rr:column "_NOME" ; rr:language "it" ]
      ] ;
      
      rr:predicateObjectMap [ 
        rr:predicate l0:_STATO ; 
        rr:objectMap [ rr:column "_STATO" ; ]
      ] ;
    	
    	rr:predicateObjectMap [ 
        rr:predicate l0:_CODICE_CATASTALE ; 
        rr:objectMap [ rr:column "_CODICE_CATASTALE" ; ]
      ] ;
      
      rr:predicateObjectMap [ 
        rr:predicate l0:_CODICE_ISTAT ; 
        rr:objectMap [ rr:column "_CODICE_ISTAT" ; ]
      ] ;
      
      rr:predicateObjectMap [ 
        rr:predicate l0:_DATA_ISTITUZIONE ; 
        rr:objectMap [ rr:column "_DATA_ISTITUZIONE" ; ]
      ] ;
      rr:predicateObjectMap [ 
        rr:predicate l0:_DATA_CESSAZIONE ; 
        rr:objectMap [ rr:column "_DATA_CESSAZIONE" ; ]
      ] ;
      rr:predicateObjectMap [ 
        rr:predicate l0:_DATA_AGGIORNAMENTO ; 
        rr:objectMap [ rr:column "_DATA_AGGIORNAMENTO" ; ]
      ] ;
      
     .
     
  """

}