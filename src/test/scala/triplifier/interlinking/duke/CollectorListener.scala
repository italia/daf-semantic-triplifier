package triplifier.interlinking.duke

import no.priv.garshol.duke.Record
import java.sql.Connection
import java.sql.DriverManager
import no.priv.garshol.duke.matchers.AbstractMatchListener
import org.slf4j.LoggerFactory

class CollectorListener extends AbstractMatchListener {

  val logger = LoggerFactory.getLogger(this.getClass)

  var count = 0

  def getMatchCount = count

  var conn: Connection = null

  override def startProcessing() {
    logger.debug("duke> LISTENER.START")
    conn = DriverManager.getConnection("jdbc:h2:file:./target/test_links.db")
    this.initialize()
  }

  override def endProcessing() {
    conn.close()
    logger.debug("duke> LISTENER.STOP")

    // final commit ?

  }

  def initialize() {

    /*
     * CHECK: extends JDBCDataSource with a custom subclass that:
     * 	+ maintains a triples table, saving triple representations for links
     * 	+ handles the commits in batch
     */

    val st = conn.createStatement()

    st.executeUpdate("DROP TABLE IF EXISTS LINKS")

    st.executeUpdate("""
      CREATE TABLE IF NOT EXISTS LINKS ( 
        `id1` VARCHAR NOT NULL, 
        `id2` VARCHAR NOT NULL, 
        `kind` INT NOT NULL, 
        `status` INT NOT NULL, 
        `timestamp` TIMESTAMP NOT NULL, 
        `confidence` FLOAT NOT NULL, 
        PRIMARY KEY (`id1`, `id2`)
      )   
    """)

    st.executeUpdate("DROP TABLE IF EXISTS triples")

    st.executeUpdate("""
      CREATE TABLE IF NOT EXISTS triples(
      	`_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
      	`sub` VARCHAR(255) NOT NULL,
      	`prp` VARCHAR(255) NOT NULL,
      	`obj` VARCHAR NOT NULL,
      	`ctx` VARCHAR(255)
      )  
    """)

    st.close()

  }

  val MAX_BATCH = 100
  var num_rec = 0

  def saveLinkOnDB(link: LINK) {

    val st = conn.createStatement()

    st.executeUpdate(s"""
      INSERT INTO triples (`sub`, `prp`, `obj`, `ctx`)
      VALUES('${link.source}', '${link.property}', '${link.target}', null)
    """)

    st.getConnection.commit()

    st.close()

  }

  override def matches(r1: Record, r2: Record, confidence: Double) {
    count += 1
    println(s"duke> MATCH: <${r1}> : <${r2}> : ${confidence}")

    val doc1 = DataSourceHelper.toMap(r1)
    val doc2 = DataSourceHelper.toMap(r2)

    val link = LINK(
      "http://www.w3.org/2002/07/owl#sameAs",
      "https://source/{_ID}", doc1,
      "https://target/{_ID}", doc2)

    logger.debug(s"LINK[${count}]: " + link.toTriple())

    // TODO: saveLinkOnDB(link)

  }

  override def matchesPerhaps(r1: Record, r2: Record, confidence: Double) {
    logger.debug(s"duke> PERHAPS: <${r1}> : <${r2}> : ${confidence}")
  }

}