//package triplifier.endpoints
//
//import javax.ws.rs.GET
//import javax.ws.rs.Path
//import javax.ws.rs.Produces
//import org.glassfish.jersey.server.ManagedAsync
//import javax.ws.rs.core.MediaType
//import javax.ws.rs.container.AsyncResponse
//import javax.ws.rs.container.Suspended
//import io.swagger.annotations.Api
//
//
//// CHECK
//@Api
//@Path("/exceptions")
//class ExceptionsTestingEndpoint {
//
//  @GET
//  @Path("/sync/example")
//  def testing_async_exception() = {
//    throw new RuntimeException("testing exceptions")
//  }
//
//  @GET
//  @Path("/async/example")
//  @ManagedAsync
//  def testing_sync_exception(@Suspended res: AsyncResponse) {
//    res.resume("ASYNC RESPONSE")
//  }
//
//}