package triplifier.main

import it.almawave.kb.http.providers._
import com.typesafe.config.ConfigFactory
import org.apache.log4j.PropertyConfigurator
import java.nio.file.Paths
import it.almawave.kb.http.HTTP
import it.almawave.kb.http.utils.JSON

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.Config
import triplifier.endpoints.RepoService

/**
 * this is just used for starting the embedded server
 */
object MainHTTPTriplifier extends App {

  // IDEA: load configuration at start

  // load log configuration from a fixed path
  PropertyConfigurator.configure("./conf/log4j.properties")

  val conf = ConfigFactory
    .parseFile(Paths.get("./conf/triplifier.conf").toAbsolutePath().normalize().toFile())
    //.parseFile(Paths.get("./conf_prod/triplifier.conf").toAbsolutePath().normalize().toFile())
    .resolve()

  val http = HTTP(conf)
  http.resource_config
    .registerInstances(new RepoService())
  // .registerInstances(new ContextListenerFilter)

  http.start

}

// TODO: add unit tests
// http://localhost:7777/kb/api/v1/swagger.json
// http://localhost:7777/kb/api/v1/application.wadl
// http://localhost:7777/static/rdf-process.html
// http://localhost:7777/static/r2rml-guess.html
