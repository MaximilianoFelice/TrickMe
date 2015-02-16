package Internals

import TrickMe.Internals.Starter.Deploy
import TrickMe.Internals.System.Start
import TrickMe.Internals.{Starter, System}
import TrickMe._
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.FunSuiteLike
import org.scalatest.concurrent.AsyncAssertions.Waiter

import scala.util.{Success, Try}

/**
 * Created by maximilianofelice on 10/02/15.
 */
class SystemInit extends TestKit(ActorSystem("InitTest")) with FunSuiteLike{

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

    var elems = Set[InitialResult]()

    var initElems = Set("elem1", "elem 2")


    def doAssertion = {assert(getNames(elems) == initElems, elems); assert(getNames(elems) != Set("hello"))}

    initStream.subscribe({elem => elems += elem}, {err => throw err},  {() => waiter{doAssertion}; waiter.dismiss})

    main.send(sys, Start(initElems))

    waiter.await()

    main.send(sys, Internals.System.Shutdown)
  }

  test("Config errors get handled properly"){
    val start = TestProbe()
    val sys = system.actorOf(Props(new System { override lazy val starter = start.ref }))
    val main = TestProbe()

    val waiter = new Waiter

    main.send(sys, System.Start(Set()))
    start.expectMsg(Deploy(Set()))

    val res: Try[String] = TrickMe.Internals.Utils.getConfigValue("NonExistent")

    assert(res.isFailure)

    main.send(sys, Internals.System.Shutdown)
  }

  // TODO: Test error cases

}
