package triplifier.endpoints.testing

import javax.ws.rs.ext.MessageBodyReader
import java.lang.annotation.Annotation
import javax.ws.rs.core.MediaType
import java.lang.reflect.Type
import com.typesafe.config.Config
import javax.ws.rs.core.MultivaluedMap
import java.io.InputStream
import scala.io.Source
import com.typesafe.config.ConfigFactory

/*
 * CHECK this example. 
 * This could be useful to handle the responses in a different way.
 * The idea is to have a component able to handle Config/Hocon response, directly.
 */
class ConfigMessageBodyReader extends MessageBodyReader[Config] {

  override def isReadable(
    `type`:      Class[_],
    genericType: Type,
    annotations: Array[Annotation],
    mediaType:   MediaType): Boolean = {
    (`type` == classOf[Config])
  }

  override def readFrom(`type`: Class[Config], genericType: Type,
                        annotations: Array[Annotation],
                        mediaType:   MediaType,
                        httpHeaders: MultivaluedMap[String, String],
                        inputStream: InputStream): Config = {

    val src = Source.fromInputStream(inputStream)
    val content = src.getLines().mkString("\n")
    src.close()

    ConfigFactory.parseString(content)

  }

}