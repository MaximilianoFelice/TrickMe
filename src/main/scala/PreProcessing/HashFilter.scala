package TrickMe
package PreProcessing

import TrickMe.Internals.Utils._
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * Created by maximilianofelice on 11/02/15.
 */

/**
 *  Generates a new stream, based on [[Internals.Starter]] initStream, which filters its results
 *  based on a MD5 Hash file comparison.
 */
object HashFilter extends TrickMeResultPublisher[Set[FileRoute]]{

  implicit val timeout = Timeout(5 seconds)

  lazy val toFilter: Set[String] = getConfigValue[Set[String]]("filteredPaths") match {
    case Success(elem) => elem
    case Failure(ex) => gotError(ex); throw ex
  }

  def getHash(path: FileRoute): String = ???

  lazy val filterRoutes = toFilter map mkabsolute

  lazy val filterFiles: Set[FileRoute] = (filterRoutes map (new java.io.File(_))) flatMap getFilesRecursively

  lazy val filterHash: Set[FileRoute] = filterFiles map getHash

  def doOnNext(res: InitialResult) = res match {
    case (pi, Failure(ex)) => publish((pi, Failure(ex)))
    case (pi, Success(elems)) => {
      val filterdElems: Set[FileRoute] = (elems map getHash) filterNot filterHash.contains

      if (!filterdElems.isEmpty) publish((pi, Success(filterdElems)))
    }
  }


  // TODO: Implement a callback method to solve companion object's lazy initialization



}

