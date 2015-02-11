package TrickMe
package Internals

import java.io.FileNotFoundException

import scala.util.{Failure, Success}

/**
 * Created by maximilianofelice on 07/02/15.
 */
trait Config {

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

      if (addr.exists && addr.isFile) (elem, Success(Set(addr.getAbsolutePath)))
      else if (addr.exists && addr.isDirectory) (elem, Success(getFilesRecursively(addr)))
      else (elem, Failure(new FileNotFoundException(s"Couldn't find path: $route")))

    } catch {

      case ex: Throwable => (elem, Failure(ex))

    }
  }

  private def getFilesRecursively(file: java.io.File): Set[String] = {
    val (dirs, files) = file.list.toList.map(new java.io.File(_)) partition {_.isDirectory}

    val res = (dirs map (_.getAbsolutePath)) :::  (dirs flatMap getFilesRecursively)

    res.toSet
  }
}