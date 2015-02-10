package TrickMe
package Internals

import akka.actor.{Actor, Props}

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by maximilianofelice on 07/02/15.
 */
object Starter extends TrickMeResultPublisher[InitialResult] {

  case class Deploy(values: Set[String])

  def props: Props = Props(new Starter with InitialStream)
}

trait Starter extends Actor {

  import TrickMe.Internals.Starter._

  def preProcess(elem: String): InitialResult

  def process(elem: String): Unit = Future{preProcess(elem)} andThen {case sth => publish(sth)} andThen {case _ => completed}

  def receive: Receive = {

    case Deploy(values) => values foreach {process(_)}

  }

}
