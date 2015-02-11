import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject

import scala.util.{Random, Failure, Success, Try}

/**
 * Created by maximilianofelice on 07/02/15.
 */
package object TrickMe {

  type Route = String
  type FileRoute = String

  /** @return  - Holds a pair containing a ProjectInfo and a set of File routes inside that project.
    *            Every item in set is ensured to be existent.
    */
  type InitialResult = (ProjectInfo, Try[Set[FileRoute]])


  /** @return - System initial stream containing [[InitialResult]] reference, composed by:
    *           ProjectInfo - The unique project identifier on the system.
    *           Routes - Containing a set of existent routes inside the Project */
  lazy val initStream: Observable[InitialResult] = TrickMe.Internals.Starter.resultStream



  /** Each instance of this class identifies a project inside the system. It is expected for each result stream to
    * provide the corresponding ProjectInfo instance for that result.
    */
  case class ProjectInfo(val projectDir: Route, val projectID: Int)


  /**
   *  Base trait to be implemented in each module. This provides system support for the streams operation.
   *
   *  It's mandatory to implement this trait in every module for each stream that will be generated. As every module
   *  is intended to have only one resultant stream, it's recommended to extend every module's companion object.
   *  In this way, it's possible to access every module result from anywhere in the system.
   *
   *  Further on, this trait will provide UI implementations.
   *
   *  @tparam T - Represents the inner type that will be published. Any type T will be streamed as a mandatory Try[T]
   *              for secure error representation. It will then be represented as a:
   *              ([[ProjectInfo]], [[Try[T]]) for streaming.
   */
  trait TrickMeResultPublisher[T] {

    /**
     *  Defines a name for the module in the current system.
     *
     *  If not defined, it gets a random 36 char string, beginning with a $
     */
    val Name: String = "$" + (new Random).nextString(36)

    /**
     *  Defines a category for the module in the current system.
     *  Default categories are:
     *    - PreProcessing
     *    - Comparison
     *    - ResultsAnalyzer
     */
    val Category: String = "Unknown"

    /**
     *  @return - The result stream of the implemented trait
     */
    val resultStream: ReplaySubject[(ProjectInfo, Try[T])] = ReplaySubject[(ProjectInfo, Try[T])]()

    /**
     *  Publishes an element in the result stream.
     *
     *  @param elem  - The element to be published in the result stream
     *
     */
    def publish(elem: (ProjectInfo, Try[T])): Unit = resultStream.onNext(elem)

    /**
     *  Overload of publish method, which instantly publishes a Success() containing the element
     *
     *  @param projectInfo - The project for which this element holds
     *
     *  @param elem - The element to be published
     *
     */
    def publish(projectInfo: ProjectInfo, elem: T): Unit = publish((projectInfo, Success(elem)))

    /**
     *  Overload of publish method, which instantly publishes a Failure() containing the exception
     *
     *  @param projectInfo - The project for which this element holds
     *
     *  @param ex - The exception to be published
     *
     */
    def publish(projectInfo: ProjectInfo, ex: Throwable): Unit = publish((projectInfo, Failure(ex)))

    /**
     *  Informs completion of the result stream. From this point on, it's assumed that no more elements will be published
     *  (none, in fact, will arrive to subscribed modules) and no further errors could be generated.
     */
    def completed = resultStream.onCompleted()

    /**
     *  Informs of a major error in the module. This module is now supposed to be now in a point of non possible recovery,
     *  which means that no further elements will be published.
     *
     * @param throwable - A [[Throwable]] element that will be handled to subscribed modules.
     */
    def gotError(throwable: Throwable) = resultStream.onError(throwable)

  }

}
