package TrickMe
package Internals

import akka.actor.Actor

/**
 * Created by maximilianofelice on 07/02/15.
 */

object System {

  case class Start(values: Set[String])

}

trait System extends Actor {

  import System._

  lazy val starter = context.actorOf(Starter.props, "Program_Starter")

  def receive: Receive = {

    case Start(values) => starter ! Starter.Deploy(values)

  }

}
