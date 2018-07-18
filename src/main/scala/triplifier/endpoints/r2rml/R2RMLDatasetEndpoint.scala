package triplifier.endpoints.r2rml

import io.swagger.annotations.Api
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.GET
import triplifier.services.SingleDatasetStore
import javax.ws.rs.PathParam
import javax.ws.rs.DefaultValue
import it.almawave.kb.http.providers.ConfigurationService
import javax.inject.Inject
import javax.ws.rs.core.Response
import java.nio.file.Paths
import io.swagger.models.parameters.BodyParameter
import javax.ws.rs.POST
import java.io.FileOutputStream
import javax.ws.rs.DELETE
import java.nio.file.Files
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import javax.ws.rs.core.MediaType
import triplifier.services.DatasetsStore
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo

@Api(tags = Array("R2RML editing"))
@Path("/triplify")
class R2RMLDataset {

  @Inject var _configuration: ConfigurationService = null

  @GET
  @Path("/r2rml/{group}/{dataset: .+?}/{fragment}.r2rml.ttl")
  @Produces(Array(MediaType.TEXT_PLAIN))
  def save_r2rml(
    @PathParam("group")@DefaultValue("daf") group:              String,
    @PathParam("dataset")@DefaultValue("testing") dataset_path: String,
    @PathParam("fragment")@DefaultValue("example") fragment:    String) = {

    val conf = _configuration.conf
    val r2rml_base_path = conf.getString("r2rml.path")

    val r2rml_path = Paths.get(s"${r2rml_base_path}/${group}/${dataset_path}/${fragment}.r2rml.ttl")
      .toAbsolutePath().normalize()

    val r2rml_content = Files.readAllLines(r2rml_path).mkString("\n")

    Response.ok(r2rml_content).build()

  }

  @POST
  @Path("/r2rml/{group}/{dataset: .+?}/{fragment}.r2rml.ttl")
  @Produces(Array(MediaType.TEXT_PLAIN))
  def save_r2rml(
    @PathParam("group")@DefaultValue("daf") group:                   String,
    @PathParam("dataset")@DefaultValue("testing") dataset_path:      String,
    @PathParam("fragment")@DefaultValue("example") fragment:         String,
    @BodyParameter @DefaultValue("#### R2RML mapping here...") r2rml:String) = {

    // REVIEW: configurations needs to be saved!!!
    val conf = _configuration.conf
    val jdbc_conf = conf.getConfig("jdbc.impala")

    // NOTE "localizedMessage": "No configuration setting found for key 'r2rml'",

    val r2rml_base_path = conf.getString("r2rml.path")

    // REVIEW
    // TODO: resolve name by conventions
    val file = Paths.get(s"${r2rml_base_path}/${group}/${dataset_path}/${fragment}.r2rml.ttl")
      .toAbsolutePath().normalize().toFile()

    if (!file.getParentFile.exists())
      file.getParentFile.mkdirs()

    val fos = new FileOutputStream(file)
    fos.write(r2rml.getBytes)
    fos.close()

    Response
      .ok(s"file ${file} saved")
      .build()

  }

  @DELETE
  @Path("/r2rml/{group}/{dataset: .+?}/{fragment}.r2rml.ttl")
  @Produces(Array(MediaType.TEXT_PLAIN))
  def delete_r2rml(
    @PathParam("group")@DefaultValue("daf") group:              String,
    @PathParam("dataset")@DefaultValue("testing") dataset_path: String,
    @PathParam("fragment")@DefaultValue("example") fragment:    String) = {

    val conf = _configuration.conf
    val r2rml_base_path = conf.getString("r2rml.path")
    val r2rml_path = Paths.get(s"${r2rml_base_path}/${group}/${dataset_path}/${fragment}.r2rml.ttl")
      .toAbsolutePath().normalize()

    val deleted = Files.deleteIfExists(r2rml_path)
    // TODO: also delete each parent folder, if empty!

    if (deleted)
      Response.ok(s"file <${r2rml_path}> was deleted!").build()
    else
      Response.ok(s"cannot delete file <${r2rml_path}>!").build()

  }

  // TODO: get a list of queries used inside the R2RML mappings...

  /**
   * this is the full mapping for a certain dataset
   */
  @GET
  @Path("/datasets/{group}/{dataset: .+?}.r2rml")
  @Produces(Array(MediaType.TEXT_PLAIN))
  def r2rml_content(
    @PathParam("group")@DefaultValue("daf") group:               String,
    @PathParam("dataset")@DefaultValue("ipa/indirizzi") dataset: String) = {

    val conf = _configuration.conf
    val fs = new SingleDatasetStore(conf, s"${group}/${dataset}")

    val list = fs.getR2RMLMappinglList()

    val content = list.map {
      case (path, r2rml) => s"\n#### source: ${path}\n${r2rml}"
    }.mkString("\n")

    Response
      .ok(content)
      .build()

  }

  // TODO: a list of possible dataset

  @GET
  @Path("/datasets")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def r2rml_datasets_list(
    @Context uri_info: UriInfo) = {

    println(s"\n\nURI INFO: ${uri_info.getAbsolutePath}")

    val base_url = uri_info.getAbsolutePath

    val conf = _configuration.conf
    val store = new DatasetsStore(conf)

    val _datasets = store
      .datasetsNamesByGroup
      .map { item =>
        val sds = item._2
        (item._1, sds.map(p => s"${base_url}/${p}.r2rml"))
      }

    Response
      .ok()
      .entity(_datasets)
      .build()

  }

  // TODO: a list of SQL views

}

