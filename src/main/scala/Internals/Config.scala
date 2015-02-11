package TrickMe
package Internals

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
  def preProcess(elem: ProjectInfo): InitialResult = ???
}