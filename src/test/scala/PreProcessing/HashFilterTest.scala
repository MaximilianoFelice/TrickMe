package PreProcessing

import TrickMe.Internals.Starter.Deploy
import TrickMe.Internals.System
import TrickMe.Internals.System.{ShutDownOperation, Start}
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfter, FunSuiteLike}

/**
 * Created by maximilianofelice on 11/02/15.
 */
class HashFilterTest extends TestKit(ActorSystem("Hash_Filter_Test")) with FunSuiteLike with BeforeAndAfter{

  trait FullConfig {
    val activeModules: Set[() => Unit] = Set()
    val filteredPaths = Set("foo")
  }

  val start = TestProbe()

  def newSystem: ActorRef = system.actorOf(Props(new System with FullConfig {
    override lazy val starter = start.ref
  }), "New_System")

  var sys: ActorRef = null
  val main = TestProbe()

  before{
    sys = newSystem
    system.eventStream.subscribe(main.ref, classOf[ShutDownOperation])
  }

  after{
    main.send(sys, System.Shutdown)
    main.expectMsg(TrickMe.Internals.System.Bye)
  }

  test("Config gets loaded properly"){
    main.send(sys, Start(Set()))
    start.expectMsg(Deploy(Set()))

    assert(HashFilter.toFilter == Set("foo"))
    assert(HashFilter.toFilter != Set("bar"))

    system.eventStream.subscribe(main.ref, classOf[ShutDownOperation])
  }

  




}
