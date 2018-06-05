package triplifier.main

import it.almawave.kb.http.providers._
import com.typesafe.config.ConfigFactory
import org.apache.log4j.PropertyConfigurator
import java.nio.file.Paths
import it.almawave.kb.http.HTTP

/**
 * this is just used for starting the embedded server
 */
object MainHTTP extends App {

  // IDEA: load configuration at start

  // load log configuration from a fixed path
  PropertyConfigurator.configure("./conf/log4j.properties")

  val conf = ConfigFactory
    .parseFile(Paths.get("./conf/application.conf").toAbsolutePath().normalize().toFile())
    .resolve()

  val http = HTTP(conf)

  http.start

}


//CHECK
//http://localhost:7777/kb/api/v1/swagger.json
//http://localhost:7777/kb/api/v1/application.wadl
