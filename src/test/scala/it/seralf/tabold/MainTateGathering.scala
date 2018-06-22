package it.seralf.tabold

import java.net.URL
import java.sql.DriverManager

object MainTateGatheringArtists extends App {

  Class.forName("org.sqlite.JDBC")

  val conn = DriverManager.getConnection("jdbc:sqlite:./db/tate_collection.db")

  val url = new URL("https://raw.githubusercontent.com/tategallery/collection/master/artist_data.csv")
  val sampled = CSVParser.sample(url.openStream(), 1, 1000000000).get
  val lines = CSVParser.parse(sampled)

  //  TODO: DDL CREATE TABLE
  val st = conn.createStatement()

  conn.setAutoCommit(false)
  conn.commit()

  st.executeUpdate(s"""
    DROP TABLE IF EXISTS artists
  """)
  conn.commit()

  st.executeUpdate(s"""
  
    CREATE TABLE IF NOT EXISTS artists (
      _ROW_NUM INT,
      id STRING,
      name STRING,
      gender STRING,
      dates STRING,
      yearOfBirth INT,
      yearOfDeath INT,
      placeOfBirth STRING,
      placeOfDeath STRING,
      url STRING
    )  
    
  """)
  conn.commit()

  lines.foreach { line =>

    // val hh = line.map(_._1)

    val _values = line.map { c =>

      val _value = c.value match {

        case txt: String => s""""${txt}""""
        case x           => x

      }

      (c.name, _value)

    }

    val query = s"""
      INSERT INTO artists  
      VALUES (${_values.map(_._2).mkString(",")}); """
    println(query)

    try {
      st.executeUpdate(query)
    } catch {
      case err: Throwable => println(err)
    }

  }

  st.executeUpdate("""
    DROP VIEW IF EXISTS artists_view
  """)

  st.executeUpdate("""
    CREATE VIEW IF NOT EXISTS artists_view AS 
    SELECT 
    SUBSTR(name,0,INSTR(name, ',')) AS _artist_last_name
    ,SUBSTR(name,INSTR(name, ',')+1) AS _artist_first_name
    ,SUBSTR(placeOfBirth,0,INSTR(placeOfBirth, ',')) AS _birth_city
    ,SUBSTR(placeOfBirth,INSTR(placeOfBirth, ',')+1) AS _birth_country
    ,*
    FROM artists
  """)

  conn.commit()

  st.closeOnCompletion()

  conn.close()

}

object MainTateGatheringArtworks extends App {

  Class.forName("org.sqlite.JDBC")

  val conn = DriverManager.getConnection("jdbc:sqlite:./db/tate_collection.db")

  val url = new URL("https://raw.githubusercontent.com/tategallery/collection/master/artwork_data.csv")
  val sampled = CSVParser.sample(url.openStream(), 1, 1000000000).get
  val lines = CSVParser.parse(sampled)

  //  lines.foreach(println(_))

  //  TODO: DDL CREATE TABLE

  val st = conn.createStatement()

  conn.setAutoCommit(false)
  conn.commit()

  st.executeUpdate("""
    DROP TABLE IF EXISTS artworks
  """)
  conn.commit()

  st.executeUpdate("""
   CREATE TABLE IF NOT EXISTS artworks (
    _ROW_NUM INT, 
    `id` STRING,
    `accession_number` STRING,
    `artist` STRING,
    `artistRole` STRING,
    `artistId` STRING,
    `title` STRING,
    `dateText` STRING,
    `medium` STRING,
    `creditLine` STRING,
    `year` STRING,
    `acquisitionYear` STRING,
    `dimensions` STRING,
    `width` STRING,
    `height` INT,
    `depth` INT,
    `units` STRING,
    `inscription` STRING,
    `thumbnailCopyright` STRING,
    `thumbnailUrl` STRING,
    `url` STRING
   )
""")
  conn.commit()

  lines.foreach { line =>

    val _values = line.map { c =>

      val _value = c.value match {

        case txt: String => s""""${txt}""""
        case x           => x

      }

      (c.name, _value)

    }

    val query = s"""
      INSERT INTO artworks  
      VALUES (${_values.map(_._2).mkString(",")}); """
    println(query)

    try {
      st.executeUpdate(query)
    } catch {
      case err: Throwable => println(err)
    }

  }

  st.executeUpdate("""
    DROP VIEW IF EXISTS artists_view
  """)

  st.executeUpdate("""
    CREATE VIEW IF NOT EXISTS artists_view AS 
    SELECT 
    SUBSTR(name,0,INSTR(name, ',')) AS _artist_last_name
    ,SUBSTR(name,INSTR(name, ',')+1) AS _artist_first_name
    ,SUBSTR(placeOfBirth,0,INSTR(placeOfBirth, ',')) AS _birth_city
    ,SUBSTR(placeOfBirth,INSTR(placeOfBirth, ',')+1) AS _birth_country
    ,*
    FROM artists
  """)

  conn.commit()

  st.closeOnCompletion()

  conn.close()

}

