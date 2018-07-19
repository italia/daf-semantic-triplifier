package triplifier.endpoints

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import javax.ws.rs.Path
import javax.inject.Singleton
import java.nio.file.Paths
import javax.ws.rs.core.Context
import javax.xml.ws.Service
import javax.inject.Inject
import org.slf4j.LoggerFactory
import javax.ws.rs.ext.Provider
import javax.ws.rs.ext.ContextResolver
import repository.triplestore.MockRepo

@Singleton
@Path("conf://repository")
class RepoService {

  val logger = LoggerFactory.getLogger(this.getClass)

  val rdf_url = Paths.get("target/DUMP/test/territorial-classifications/regions.ttl")
    .toAbsolutePath().normalize()
    .toUri().toURL()

  println("####################")
  logger.debug(s"#### REPOSITORY SERVICE")

  val repo = new MockRepo()
  repo.start()

  //  println("\nMOCK1 @ " + this.hashCode())

  // LOADING EXAMPLE DATA
  //  repo.load(rdf_url)

  // QUERY ON EXAMPLE DATA
  //  println("EXAMPLE QUERY.....................")
  //  repo.endpoint.query("""SELECT DISTINCT ?concept WHERE { ?s a ?concept }""", "csv")(System.out)
  //  println("EXAMPLE QUERY.....................")

}