package PreProcessing

import java.io.{File, FileInputStream}

import TrickMe.Internals.Utils._
import FileOpener._
import TrickMe._
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
object HashFilter extends TrickMeResultPublisher[Set[File]]{

  implicit val timeout = Timeout(5 seconds)

  lazy val toFilter: Set[String] = getConfigValue[Set[String]]("filteredPaths") match {
    case Success(elem) => elem
    case Failure(ex) => gotError(ex); throw ex
  }

  def getHash(path: java.io.File): String = {
    val stream = new FileInputStream(path)
    val res = org.apache.commons.codec.digest.DigestUtils.md5Hex(stream)
    stream.close()
    res
  }

  lazy val filterRoutes = toFilter map mkabsolute

  lazy val filterFiles: Set[FileRoute] = (filterRoutes map (new java.io.File(_))) flatMap getFilesRecursively

  lazy val filterHash: Set[String] = (filterFiles map (new java.io.File(_))) map getHash

  def doOnNext(res: OpenProject) = res match {
    case (pi, Failure(ex)) => publish(pi, ex)
    case (pi, Success(elems)) => {
      val filter: File => Boolean = getHash _ andThen filterHash.contains

      val filterdElems: Set[File] = elems filterNot filter

      if (!filterdElems.isEmpty) publish((pi, Success(filterdElems)))
    }
  }


  // TODO: Implement a callback method to solve companion object's lazy initialization



}
