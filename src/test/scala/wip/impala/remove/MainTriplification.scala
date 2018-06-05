package wip.impala.remove

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import scala.collection.JavaConversions._
import scala.collection.JavaConversions.asScalaBuffer
import scala.concurrent.duration.Duration
import scala.io.Source
import scala.util.Random

import org.openrdf.model.Model
import org.openrdf.model.Statement
import org.openrdf.model.impl.LinkedHashModel
import org.openrdf.rio.RDFFormat
import org.openrdf.rio.RDFParser
import org.openrdf.rio.Rio
import org.openrdf.rio.helpers.StatementCollector
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLOntology
import org.slf4j.LoggerFactory

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import info.aduna.iteration.Iterations
import it.unibz.inf.ontop.owlapi.bootstrapping.DirectMappingBootstrapper
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences
import it.unibz.inf.ontop.sesame.RepositoryConnection
import it.unibz.inf.ontop.sesame.SesameVirtualRepo

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import java.io.OutputStream
import java.io.FileInputStream

// REVIEW
object MainTriplification extends App {

  val dump_file = "target/EXPORT/anpr/regioni_v02.ttl"
  val r2rml_file = "src/test/resources/r2rml/anpr/regioni_v02.r2rml2.ttl"
  val meta_file = "src/test/resources/r2rml/anpr/regioni_v02.meta.ttl"

  // TODO: capire rallentamenti / problemi di connettività con Impala di DAF!!!

  val ontop = Ontop

  val ok = ontop.processR2RML(r2rml_file) { dump_statements =>

    ontop.writeToOutputStream(dump_statements, System.out)

  }

  val dump = ontop.previewDump(dump_file)
  println(dump)

}
