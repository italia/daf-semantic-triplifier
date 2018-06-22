package triplifier.endpoints

object MainFileWithCharset extends App {

  val fs = new DatasetHelper("./data", "sqlite-test/territorial-classifications/regions")

  val content = fs.createRDFDump("ttl")
  println(content)

}