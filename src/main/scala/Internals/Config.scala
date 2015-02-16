package TrickMe
package Internals

import java.io.FileNotFoundException

import PreProcessing.HashFilter

import scala.util.{Failure, Success}

/**
 * Created by maximilianofelice on 07/02/15.
 */
trait Config {

  val activeModules: Set[Function0[Unit]] = Set(HashFilter.preStart _)
  val filteredPaths = Set("hola")
}

/**
 *  Gets the initial stream configuration
 */
trait InitialStream {

  /**
   *  The function that will handle initial stream configuration
   * @param elem  - A ProjectInfo, provided by the system, that contain the project's specification.
   * @return      - A Set of InitialResults, that will represent every initial publication
   */
  def preProcess(elem: ProjectInfo): InitialResult = {
    try {

      val route = elem.projectDir
      val addr = new java.io.File(route)

      if (addr.exists) (elem, Success(Internals.Utils.getFilesRecursively(addr)))
      else (elem, Failure(new FileNotFoundException(s"Couldn't find path: $route")))

    } catch {

      case ex: Throwable => (elem, Failure(ex))

    }
  }

}
