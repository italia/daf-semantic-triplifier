//package triplifier.endpoints
//
//import javax.ws.rs.container.ContainerResponseFilter
//import javax.ws.rs.ext.Provider
//import javax.ws.rs.container.ContainerRequestContext
//import javax.ws.rs.container.ContainerResponseContext
//
///*
// * TODO: review this class
// */
//@Provider
//class CacheControlFilter extends ContainerResponseFilter {
//
//  def filter(req: ContainerRequestContext, res: ContainerResponseContext) {
//    if (req.getMethod().equals("GET")) {
//      req.getHeaders().add("Cache-Control", "max-age=120");
//    }
//  }
//
//}
