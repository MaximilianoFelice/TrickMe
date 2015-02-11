package TrickMe
package Internals

import akka.actor.{Actor, Props}
import rx.lang.scala.Subject

/**
 * Created by maximilianofelice on 07/02/15.
 */

object System {

  /**
   *  Indicate initial startup
   *  
   *  @param values - Initial system values
   */
  case class Start(values: Set[Route])

  /**
   *  Message that indicates that an instance of the system should shut down.
   */
  case object Shutdown

  /**
   *  Subject that will publish reset callbacks registration for every module
   */
  val resetCallback = Subject[Function0[Unit]]()

  /**
   *  Will return sequential ID's for every project in the system.
   */
  private var nextID = 0
  def nextVal = {val id = nextID; nextID += 1; id}

  def props: Props = Props(new System{})
}

trait System extends Actor {

  import TrickMe.Internals.System._
  
  /**  @return - The starter Actor used to get the initial stream of the system */
  lazy val starter = context.actorOf(Starter.props, "Program_Starter")


  /**
   *  Variable that will hold callbacks to be called at shutting down
   */
  private var activeCallbacks = Set[Function0[Unit]]()
  resetCallback.subscribe(cb => activeCallbacks += cb)


  /**
   *  @return - Set of active streams
   */
  var activeStreams = Set[TrickMeResultPublisher[Any]]()
  
  /**
   *  Generates [[ProjectInfo]] instance for a route
   * @return - A projectInfo instance for the route.
   */
  def generateProjectInfo(route: String): ProjectInfo = ProjectInfo(route, nextID)
  
  def receive: Receive = {

    /**
     *  System entry point. Will receive values containing initial elements.
     */
    case Start(values) => starter ! Starter.Deploy(values map generateProjectInfo)

    /**
     *  Shutdowns the system
     */
    case Shutdown => context.children foreach (_ ! System.Shutdown); activeCallbacks foreach {_()}; context.stop(self)
  }

}
