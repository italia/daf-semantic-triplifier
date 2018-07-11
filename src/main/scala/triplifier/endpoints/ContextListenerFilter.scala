//package triplifier.endpoints
//
//import javax.ws.rs.container.ContainerResponseFilter
//import javax.servlet.ServletContextListener
//import javax.servlet.ServletContextEvent
//import javax.ws.rs.ext.Provider
//import org.slf4j.LoggerFactory
//import javax.servlet.annotation.WebListener
//
//@WebListener
//class ContextListenerFilter extends ServletContextListener {
//
//  val logger = LoggerFactory.getLogger(this.getClass)
//
//  def contextInitialized(event: ServletContextEvent) {
//    logger.info(s"....APPLICATION CONTEXT INITIALIZED")
//  }
//
//  def contextDestroyed(event: ServletContextEvent) {
//    logger.info(s"....APPLICATION CONTEXT DESTROYED")
//  }
//
//}
//
