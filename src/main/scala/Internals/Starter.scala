package TrickMe
package Internals

import akka.actor.{Actor, Props, Stash}
import akka.pattern.pipe

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by maximilianofelice on 07/02/15.
 */

/**
 *  Represents the [[Actor]] that will handle the initial data stream
 */
object Starter extends TrickMeResultPublisher[Set[FileRoute]] {

  override val Name = "Starter"
  
  override val Category = "PreProcessing"

  case class Failure(projectInfo: ProjectInfo, ex: Throwable)

  case class Deploy(values: Set[ProjectInfo])

  def props: Props = Props(new Starter with InitialStream)
}

/**
 *  Represents a Starter Actor, that will process and publish initial data stream.
 */
trait Starter extends Actor with Stash{

  import TrickMe.Internals.Starter._


  def preProcess(elem: ProjectInfo): InitialResult

  def process(elem: ProjectInfo): Unit = Future{preProcess(elem)} recover {case ex => self ! Starter.Failure(elem, ex)} pipeTo self

  def receive: Receive = {

    case Deploy(values) => context.become(waitingResults(values.size)); values foreach {process(_)}

    case System.Shutdown => context.stop(self)

  }

  def waitingResults(still: Int): Receive = PartialFunction[Any,Unit]{

    case results: InitialResult => publish(results)

    case Starter.Failure(pinfo, ex) => publish(pinfo, ex)

  } andThen {

    /**
     *  Error case, we started waiting non existent results
     */
    case _ if still == 0 => stash; context.become(receive); unstashAll

    case _ if still-1 == 0 => context.become(receive); completed; unstashAll

    case _ => context.become(waitingResults(still -1))

  } orElse {

    /**
     *  In case of a non handled message, we stash it for later handling
     */
    case _ => stash
  }

}
