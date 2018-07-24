package triplifier.interlinking.duke

import java.sql.DriverManager

object MainH2Test extends App {

  Class.forName("org.h2.Driver")

  val conn = DriverManager.getConnection("jdbc:h2:file:./target/test_links.db")
  val st = conn.createStatement()

  st.executeUpdate("""
    CREATE TABLE IF NOT EXISTS LINKS ( 
    `id1` VARCHAR NOT NULL, 
    `id2` VARCHAR NOT NULL, 
    `kind` INT NOT NULL, 
    `status` INT NOT NULL, 
    `timestamp` TIMESTAMP NOT NULL, 
    `confidence` FLOAT NOT NULL, 
    PRIMARY KEY (`id1`, `id2`))   
  """)
  
  st.close()
  conn.close()

}