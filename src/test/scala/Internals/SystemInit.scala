package Internals

import TrickMe.Internals.Starter
import TrickMe.Internals.System.Start
import TrickMe._
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.FunSuiteLike
import org.scalatest.concurrent.AsyncAssertions.Waiter

import scala.util.Success

/**
 * Created by maximilianofelice on 10/02/15.
 */
class SystemInit extends TestKit(ActorSystem("InitTest")) with FunSuiteLike {

  trait TestInitStream {
    def preProcess(elem: ProjectInfo): InitialResult = (elem, Success(Set()))
  }

  def getNames(result: Set[InitialResult]): Set[Route] = result map {_._1.projectDir}

  def newSystem: ActorRef = system.actorOf(Props( new Internals.System{
    override lazy val starter = context.actorOf(Props(new Starter with TestInitStream))
  } ) )

  test("System Initializes correctly"){
    val sys = newSystem
    val main = TestProbe()

    val waiter = new Waiter

    var elems = List[InitialResult]()

    var initElems = Set("elem1", "elem 2")


    def doAssertion = assert(getNames(elems.toSet) == initElems)

    initStream.subscribe({elem => elems = elem :: elems}, {err => throw err},  {() => waiter{doAssertion}; waiter.dismiss})

    main.send(sys, Start(initElems))

    waiter.await()

  }

  // TODO: Test error cases

}
