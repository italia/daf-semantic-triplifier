package triplifier.endpoints

import io.swagger.annotations.Api
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.GET
import javax.ws.rs.core.MediaType
import triplifier.services.DatasetsStore
import javax.ws.rs.PathParam
import javax.ws.rs.DefaultValue
import it.almawave.kb.http.providers.ConfigurationService
import javax.inject.Inject
import javax.ws.rs.core.Response

@Api(tags = Array("RDF processor"))
@Path("/r2rml")
class R2RMLDataset {

  @Inject var _configuration: ConfigurationService = null

  @GET
  @Path("/datasets/{group}/{dataset: .+?}")
  @Produces(Array(MediaType.TEXT_PLAIN))
  def list(
    @PathParam("group")@DefaultValue("daf") group:                    String,
    @PathParam("dataset")@DefaultValue("ipa/indirizzi") dataset: String) = {

    val conf = _configuration.conf
    val fs = new DatasetsStore(conf, s"${group}/${dataset}")

    val list = fs.getR2RMLMappinglList()

    println("#######################")
    println(list.mkString("\n"))

    Response
      .ok(list.mkString("\n"))
      .build()

  }

}

