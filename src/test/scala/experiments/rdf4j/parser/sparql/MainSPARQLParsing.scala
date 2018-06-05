package experiments.rdf4j.parser.sparql

import org.openrdf.query.QueryLanguage
import org.openrdf.query.parser.QueryParserUtil
import org.openrdf.query.parser.ParsedQuery
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer
import org.openrdf.query.parser.sparql.ast.SyntaxTreeBuilder

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

// CHECK: SPARQL parsing on tree
object MainSPARQLParsing extends App {

  val baseURI = "http://example.org"
  val queryString = """
    PREFIX foaf: <http://xmlns.com/foaf/0.1/> 
    SELECT DISTINCT ?first_name ?last_name   
    WHERE { 
      ?person a foaf:Person .
      ?person foaf:firstName ?first_name . 
      ?person foaf:lastName ?last_name .
    }
  """

  val pq: ParsedQuery = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, queryString, baseURI)

  val parseTree: ASTQueryContainer = SyntaxTreeBuilder.parseQuery(queryString)

  val nodes = parseTree.jjtGetChildren().toList

  nodes.foreach { node =>
    println(node)
  }

  //  TODO: JSON marshalling of tree

}