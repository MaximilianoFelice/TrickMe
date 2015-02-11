package TrickMe
package Internals

import akka.actor.{Props, Actor}

/**
 * Created by maximilianofelice on 07/02/15.
 */

object System {

  case class Start(values: Set[Route])

  case object Shutdown

  var projects = Set[ProjectInfo]()


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
    case Shutdown => context.stop(self)
  }

}
