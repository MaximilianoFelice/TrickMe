package PreProcessing

import TrickMe.Internals.Starter.Deploy
import TrickMe.Internals.System
import TrickMe.Internals.System.{ShutDownOperation, Start}
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.concurrent.AsyncAssertions.Waiter
import org.scalatest.{BeforeAndAfter, FunSuiteLike}

import scala.concurrent.duration._

import TrickMe._

/**
 * Created by maximilianofelice on 11/02/15.
 */
class HashFilterTest extends TestKit(ActorSystem("Hash_Filter_Test")) with FunSuiteLike with BeforeAndAfter{

  trait FullConfig {
    val activeModules: Set[() => Unit] = Set()
    val filteredPaths = Set("/src/test/scala/TestCode/so-test-sockets/makefile")
  }

  val makefileHash = "0b2e7256362de901a97de587e54290b8"

  val start = TestProbe()

  def newSystem: ActorRef = system.actorOf(Props(new System with FullConfig {
    override lazy val starter = start.ref
  }), "New_System")

  var sys: ActorRef = null
  val main = TestProbe()

  before{
    sys = newSystem
  }

  after{
    main watch sys
    main.send(sys, System.Shutdown)
    main.expectMsg(TrickMe.Internals.System.Bye)
    main.expectTerminated(sys, 5 seconds)
  }

  test("Config gets loaded properly"){
    main.send(sys, Start(Set()))
    start.expectMsg(Deploy(Set()))

    assert(HashFilter.toFilter == Set("/src/test/scala/TestCode/so-test-sockets/makefile"))
    assert(HashFilter.toFilter != Set("Bar"))

    system.eventStream.subscribe(main.ref, classOf[ShutDownOperation])
  }

  test("Hash gets loaded properly"){

    val makeRoute = TrickMe.Internals.Utils.mkabsolute("/src/test/scala/TestCode/so-test-sockets/makefile")

    assert(HashFilter.getHash(new java.io.File(makeRoute)) == makefileHash)
  }

  test("Hash Filter gets hashes to filter correctly"){
    assert(HashFilter.filterHash.contains(makefileHash))
  }

  test("Hash Filter filters all neccesary paths"){
    var results = Set[OpenProject]()

    val projectPath = new java.io.File("").getAbsolutePath
    val testKitPath = "/src/test/scala/TestCode/so-test-sockets/"
    val absDir = projectPath + testKitPath

    val waiter = new Waiter

    main.send(sys, Start(Set(absDir)))

    def doAssertions = {
      val elem: OpenProject = results.head
      assert(elem._2.isSuccess)
      val paths = elem._2.get map (_.getAbsolutePath)
      assert(!paths.contains("/src/test/scala/TestCode/so-test-sockets/makefile"))
      assert(paths.contains(projectPath + "Cliente/src/Cliente.c"))
    }

    HashFilter.resultStream.subscribe(
      onNext = {elem => results += elem},
      onError = {err => throw err},
      onCompleted = {() => waiter(doAssertions); waiter.dismiss} )



  }



}
