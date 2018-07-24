package triplifier.interlinking.duke

import no.priv.garshol.duke.matchers.LinkDatabaseMatchListener
import java.util.Properties
import no.priv.garshol.duke.JDBCLinkDatabase
import no.priv.garshol.duke.Processor
import no.priv.garshol.duke.ConfigLoader

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import no.priv.garshol.duke.matchers.PrintMatchListener

object MainMinimalDukeConfig extends App {

  val config = ConfigLoader.load("./target/duke/duke_example.xml")
  config.validate()

  val props = config.getProperties

  val processor = new Processor(config)

  val db_links = new JDBCLinkDatabase(
    "org.h2.Driver",
    "jdbc:h2:file:./target/duke/test_links.db",
    "h2", new Properties())

  val linkListener = new LinkDatabaseMatchListener(config, db_links)
  processor.addMatchListener(linkListener)

//  val collectorListener = new CollectorListener
//  processor.addMatchListener(collectorListener)

  // LOG to console
  val printMatchListener = new PrintMatchListener(true, true, true, true, props, true)
  processor.addMatchListener(printMatchListener)

  processor.link(
    config.getDataSources(1),
    config.getDataSources(2),
    false,
    1000)

  db_links.getAllLinks
    .zipWithIndex
    .foreach {
      case (link, i) =>
        println(i + ": " + link)
    }

  processor.close()

}