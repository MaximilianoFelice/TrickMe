package TrickMe
package Internals

import akka.actor.{Actor, ActorRef, Props}
import rx.lang.scala.Subject

import scala.util.{Success, Failure, Try}

/**
 * Created by maximilianofelice on 07/02/15.
 */

object System {

  trait ShutDownOperation
  /**
   *  Indicate initial startup
   *  
   *  @param values - Initial system values
   */
  case class Start(values: Set[Route])

  /**
   *  Requests a value from config to the system
   *
   *  @param value - Value name
   *
   */
  case class Config(value: String)

  /**
   *  Message that indicates that an instance of the system should shut down.
   */
  case object Shutdown extends ShutDownOperation

  /**
   *  Message that indicates that an instance of the system ended.
   */
  case object Bye extends ShutDownOperation

  /**
   *  Subject that will publish reset callbacks registration for every module
   */
  val resetCallback = Subject[Function0[Unit]]()

  /**
   *  Will return sequential ID's for every project in the system.
   */
  private var nextID = 0
  protected def nextVal = {val id = nextID; nextID += 1; id}

  def props: Props = Props(new System{})

  protected var currSys: Option[ActorRef] = None
  def currentSystem = currSys
}

trait System extends Actor {

  import TrickMe.Internals.System._

  require{currSys == None}
  override def preStart = {
    currSys = Some(self)
    //startModules
  }

  /**
   *  Module startup: Calls functions in Config: ActiveModules
   */
  private def startModules: Unit = {
    val funcs: Try[Set[() => Unit]] = getValue("activeModules").asInstanceOf[Try[Set[() => Unit]]]
    funcs match {
      case Success(funcs) => funcs foreach (_())
      case Failure(_) => () /* No modules up */
    }
  }
  
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
  private def generateProjectInfo(route: String): ProjectInfo = ProjectInfo(route, nextID)

  /**
   *  Gets a config value by reflection.
   *
   *  @param value  - Config accessor name
   *
   *  @return       - A Try[AnyRef] with the asked value, or an error
   */
  private def getValue(value: String): Try[AnyRef] = Try{
    val field = this.getClass.getDeclaredField(value)
    field.setAccessible(true)
    val x = field.get(this)
    x
  }

  def receive: Receive = {

    /**
     *  System entry point. Will receive values containing initial elements.
     */
    case Start(values) => starter ! Starter.Deploy(values map generateProjectInfo)

    /**
     *  Shutdowns the system
     */
    case Shutdown => context.children foreach (_ ! System.Shutdown); activeCallbacks foreach {_()}; currSys = None; context.stop(self)

    /**
     *  Answers any Configuration name ask with its corresponding value
     */
    case Config(name) => sender ! getValue(name)
  }

  override def postStop = {context.system.eventStream.publish(Bye)}

}
