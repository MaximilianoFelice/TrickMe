import TrickMe.Internals.Starter
import TrickMe.Internals.System.Start
import TrickMe._
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestProbe, TestKit}
import org.scalatest.FunSuiteLike
import org.scalatest.concurrent.AsyncAssertions.Waiter

import scala.util.{Success, Try}

/**
 * Created by maximilianofelice on 10/02/15.
 */
class SystemInit extends TestKit(ActorSystem("InitTest")) with FunSuiteLike {

  trait TestInitStream {
    def preProcess(elem: String): InitialResult = (elem, List())
  }

  def newSystem: ActorRef = system.actorOf(Props( new Internals.System{
    override lazy val starter = context.actorOf(Props(new Starter with TestInitStream))
  } ) )

  test("System Initializes correctly"){
    val sys = newSystem
    val main = TestProbe()

    val waiter = new Waiter

    var elems = List[Try[InitialResult]]()

    def doAssertion = assert(elems.toSet == Set(Success("elem1", List()), Success("elem 2", List())))

    initStream.subscribe({elem => elems = elem :: elems}, {err => throw err},  {() => waiter{doAssertion}; waiter.dismiss})

    main.send(sys, Start(Set("elem1", "elem 2")))

    waiter.await()

  }

}
