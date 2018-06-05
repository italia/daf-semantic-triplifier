
example ontop SQL query construction from R2RML
===================================================



Given the following example R2RML mapping:

```turtle

@prefix rr: <http://www.w3.org/ns/r2rml#> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix l0: <https://w3id.org/italia/onto/l0/> .
@prefix clvapit: <https://w3id.org/italia/onto/CLV/> .

@base  <https://w3id.org/italia/> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .


<VIEW_regioni> rr:sqlQuery """

  SELECT
	codice_regione AS _CODICE_REGIONE
	,denominazione_regione AS _NOME_REGIONE
	,codice_nuts2_2006 AS _NUTS2
  FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani'

"""
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

```

the following SQL is created:

```sql

SELECT
   1 AS "sQuestType", NULL AS "sLang", ('https://w3id.org/italia/controlled-vocabulary/territorial-classifications/regions/' || CAST(QVIEW1._CODICE_REGIONE AS CHAR)) AS "s",
   1 AS "pQuestType", NULL AS "pLang", 'https://w3id.org/italia/onto/l0/name' AS "p", 
   3 AS "oQuestType", 'it' AS "oLang", CAST(QVIEW1._NOME_REGIONE AS CHAR) AS "o"
 FROM 
(SELECT 
	codice_regione AS _CODICE_REGIONE 
	,denominazione_regione AS _NOME_REGIONE 
	,codice_nuts2_2006 AS _NUTS2  
  FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani') QVIEW1
WHERE 
QVIEW1._CODICE_REGIONE IS NOT NULL AND
QVIEW1._NOME_REGIONE IS NOT NULL
UNION ALL
SELECT 
   1 AS "sQuestType", NULL AS "sLang", ('https://w3id.org/italia/controlled-vocabulary/territorial-classifications/regions/' || CAST(QVIEW1._CODICE_REGIONE AS CHAR)) AS "s", 
   1 AS "pQuestType", NULL AS "pLang", 'https://w3id.org/italia/onto/CLV/situatedWithin' AS "p", 
   1 AS "oQuestType", NULL AS "oLang", 'https://w3id.org/italia/controlled-vocabulary/territorial-classifications/countries/ITA' AS "o"
 FROM 
(SELECT 
	codice_regione AS _CODICE_REGIONE 
	,denominazione_regione AS _NOME_REGIONE 
	,codice_nuts2_2006 AS _NUTS2  
  FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani') QVIEW1
WHERE 
QVIEW1._CODICE_REGIONE IS NOT NULL
UNION ALL
SELECT 
   1 AS "sQuestType", NULL AS "sLang", ('https://w3id.org/italia/controlled-vocabulary/territorial-classifications/regions/' || CAST(QVIEW1._CODICE_REGIONE AS CHAR)) AS "s", 
   1 AS "pQuestType", NULL AS "pLang", 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' AS "p", 
   1 AS "oQuestType", NULL AS "oLang", 'https://w3id.org/italia/onto/CLV/Region' AS "o"
 FROM 
(SELECT 
	codice_regione AS _CODICE_REGIONE 
	,denominazione_regione AS _NOME_REGIONE 
	,codice_nuts2_2006 AS _NUTS2  
  FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani') QVIEW1
WHERE 
QVIEW1._CODICE_REGIONE IS NOT NULL
UNION ALL
SELECT 
   1 AS "sQuestType", NULL AS "sLang", ('https://w3id.org/italia/controlled-vocabulary/territorial-classifications/regions/' || CAST(QVIEW1._CODICE_REGIONE AS CHAR)) AS "s", 
   1 AS "pQuestType", NULL AS "pLang", 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' AS "p", 
   1 AS "oQuestType", NULL AS "oLang", 'http://www.w3.org/2004/02/skos/core#Concept' AS "o"
 FROM 
(SELECT 
	codice_regione AS _CODICE_REGIONE 
	,denominazione_regione AS _NOME_REGIONE 
	,codice_nuts2_2006 AS _NUTS2  
  FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani') QVIEW1
WHERE 
QVIEW1._CODICE_REGIONE IS NOT NULL
UNION ALL
SELECT 
   1 AS "sQuestType", NULL AS "sLang", ('https://w3id.org/italia/controlled-vocabulary/territorial-classifications/regions/' || CAST(QVIEW1._CODICE_REGIONE AS CHAR)) AS "s", 
   1 AS "pQuestType", NULL AS "pLang", 'http://www.w3.org/2002/07/owl#sameAs' AS "p", 
   1 AS "oQuestType", NULL AS "oLang", ('http://nuts.geovocab.org/id/' || CAST(QVIEW1._NUTS2 AS CHAR)) AS "o"
 FROM 
(SELECT 
	codice_regione AS _CODICE_REGIONE 
	,denominazione_regione AS _NOME_REGIONE 
	,codice_nuts2_2006 AS _NUTS2  
  FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani') QVIEW1
WHERE 
QVIEW1._CODICE_REGIONE IS NOT NULL AND
QVIEW1._NUTS2 IS NOT NULL

```

In the above R2RML example, we avoid the direct reference to a table (thus an equivalent `SELECT * FROM <table>` query), preferring instead the usage of an SQL view.

In particular the `logicalTable` was constructed from the `sqlQuery`:
```sql
SELECT
	codice_regione AS _CODICE_REGIONE
	,denominazione_regione AS _NOME_REGIONE
	,codice_nuts2_2006 AS _NUTS2
  FROM 'gove__amministrazione.default_org_o_istat_elenco_comuni_italiani'
```
which is the same query actually used in each `FROM` statement of the created SQL complete query.





