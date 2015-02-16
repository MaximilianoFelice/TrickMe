package PreProcessing

import java.io.File

import TrickMe._

import scala.util.{Failure, Success}

/**
 * Created by maximilianofelice on 16/02/15.
 */
object FileOpener extends TrickMe.TrickMeResultPublisher[Set[File]] {

  initStream.subscribe{ elem => elem match {
    case (pi, Success(routes)) => publish(pi, routes map (new File(_)))
    case (pi, Failure(ex)) => publish(pi, ex)
  }
  }
}