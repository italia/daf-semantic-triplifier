
```sparql
DROP GRAPH <daf://ipa>
;
LOAD <file:///C:/Users/Al.Serafini/repos/DAF/triplifier/target/DUMP/daf/ipa/incarichi.ttl>
INTO GRAPH <daf://ipa>
;
LOAD <file:///C:/Users/Al.Serafini/repos/DAF/triplifier/target/DUMP/daf/ipa/luoghi.ttl>
INTO GRAPH <daf://ipa>
;
LOAD <file:///C:/Users/Al.Serafini/repos/DAF/triplifier/target/DUMP/daf/ipa/organizzazioni.ttl>
INTO GRAPH <daf://ipa>
;
LOAD <file:///C:/Users/Al.Serafini/repos/DAF/triplifier/target/DUMP/daf/ipa/tipologie-istat.ttl>
INTO GRAPH <daf://ipa>
;
LOAD <file:///C:/Users/Al.Serafini/repos/DAF/triplifier/target/DUMP/daf/ipa/tipologie.ttl>
INTO GRAPH <daf://ipa>
;
```

```sparql
SELECT ?g (COUNT(?p) AS ?triples)
WHERE {

  GRAPH ?g {
  
    ?s ?p ?o
  
  }

}
GROUP BY ?g 
```