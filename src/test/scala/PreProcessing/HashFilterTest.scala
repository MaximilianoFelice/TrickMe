package PreProcessing

import TrickMe.Internals.Starter.Deploy
import TrickMe.Internals.System
import TrickMe.Internals.System.{ShutDownOperation, Start}
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.FunSuiteLike

/**
 * Created by maximilianofelice on 11/02/15.
 */
class HashFilterTest extends TestKit(ActorSystem("Hash_Filter_Test")) with FunSuiteLike {

  trait FullConfig {
    val activeModules: Set[() => Unit] = Set()
    val filteredPaths = Set("foo")
  }

  val start = TestProbe()

  def newSystem: ActorRef = system.actorOf(Props(new System with FullConfig {
    override lazy val starter = start.ref
  }), "New_System")

  test("Config gets loaded properly"){
    val sys = newSystem
    val main = TestProbe()

    main.send(sys, Start(Set()))
    start.expectMsg(Deploy(Set()))

    assert(HashFilter.toFilter == Set("foo"))
    assert(HashFilter.toFilter != Set("bar"))

    system.eventStream.subscribe(main.ref, classOf[ShutDownOperation])
    main.send(sys, System.Shutdown)

    main.expectMsg(TrickMe.Internals.System.Bye)
  }


}
