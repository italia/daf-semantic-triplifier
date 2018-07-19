package repository.triplestore

import org.eclipse.rdf4j.sail.inferencer.util.RDFInferencerInserter
import org.openrdf.repository.util.RDFInserter
import org.openrdf.repository.sparql.SPARQLRepository
import java.nio.file.Paths
import org.openrdf.rio.RDFFormat
import org.openrdf.rio.Rio

/**
 * examples to check insertion on external endpoint, in a standard way.
 * NOTE: this should be a test 
 */
object MainInserter extends App {

  //  val repo = new SPARQLRepository("http://localhost:9999/blazegraph/sparql")
  val repo = new SPARQLRepository("http://localhost:8890/sparql")
  repo.initialize()

  val conn = repo.getConnection
  val vf = conn.getValueFactory
  val ctx = vf.createURI("https://testing/")

  //  val inserter = new RDFInserter(conn)
  //  inserter.enforceContext(ctx)

  val rdf_url = Paths.get("target/DUMP/test/territorial-classifications/regions.ttl")
    .toAbsolutePath().normalize()
    .toUri().toURL()

  conn.begin()
  conn.add(rdf_url, ctx.toString(), Rio.getParserFormatForFileName(rdf_url.toString()), ctx)
  conn.commit()

  conn.close()

}

/*
NOTE: "Use an empty array (not null!) to indicate no context(s) should be enforced."
*/