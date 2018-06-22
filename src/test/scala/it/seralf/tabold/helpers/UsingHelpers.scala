package it.seralf.tabold.helpers

import scala.util.control.NonFatal
import scala.util._

object UsingHelpers {

  object using {

    def apply[IN, OUT](resource: IN)(post_action: IN => Unit)(action: IN => OUT): Try[OUT] = {
      try {
        println("\n\ngetting data...")
        Success(action(resource))
      } catch {
        case e: Throwable =>
          println("errors...")
          Failure(e)
      } finally {
        println("trying closing...")
        import scala.util.control.Exception.allCatch

        val can_post = (allCatch opt post_action(resource)).isDefined
        if (can_post) {
          try {
            if (resource != null) {
              post_action(resource)
            }
          } catch {
            case e: Throwable =>
              println(e) // should be logged
          }
        } else {
          println("finished")
        }

      }
    }

  }

  object using_and_close {

    def apply[IN, OUT](resource: IN { def close() })(action: IN => OUT): Try[OUT] = {
      using(resource)(_.close())(action)
    }

  }

  object TryWith {
    def apply[IN <: { def close() }, OUT](resource: => IN)(action: IN => OUT): Try[OUT] =

      Try(resource).flatMap(input => {
        try {

          Success(action(input))

        } catch {
          case NonFatal(ex_action) =>
            try {
              input.close()
              Failure(ex_action)
            } catch {
              case NonFatal(ex_close) =>
                ex_action.addSuppressed(ex_close) // hack!
                Failure(ex_action)
            }
        }
      })

  }

}