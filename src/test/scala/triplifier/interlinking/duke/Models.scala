package triplifier.interlinking.duke

case class LINK(
  property:          String,
  uriTemplateSource: String,
  source:            Map[String, Any],
  uriTemplateTarget: String,
  target:            Map[String, Any]) {

  def toTriple(): Triple = {

    var source_uri = uriTemplateSource
    source.toStream.foreach { e =>
      source_uri = source_uri.replace(s"{${e._1}}", e._2.toString())
    }

    var target_uri = uriTemplateTarget
    target.toStream.foreach { e =>
      target_uri = target_uri.replace(s"{${e._1}}", e._2.toString())
    }

    Triple(source_uri, property, target_uri)

  }

  override def toString() = {
    val triple = this.toTriple()
    s"<${triple.sub}> ${triple.prp} <${triple.obj}> ."
  }

}

case class Triple(
  sub: String,
  prp: String,
  obj: String,
  ctx: Option[String] = None)