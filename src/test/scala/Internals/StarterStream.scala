package Internals

import TrickMe._
import TrickMe.Internals.System
import akka.actor.ActorSystem
import akka.testkit.{TestProbe, TestKit}
import org.scalatest.FunSuiteLike
import org.scalatest.concurrent.AsyncAssertions.Waiter

import scala.util.Success

/**
 * Created by maximilianofelice on 11/02/15.
 */
class StarterStream extends TestKit(ActorSystem("Starter_Stream_Tests")) with FunSuiteLike {

  test("Starter streams correctly"){
    val sys = system.actorOf(System.props, "System")
    val main = TestProbe()
    val waiter = new Waiter

    val projectPath = new java.io.File("").getAbsolutePath
    val testKitPath = "/src/test/scala/TestCode/so-test-sockets/"
    val absDir = projectPath + testKitPath

    var results = Set[InitialResult]()

    val expectedRoutes = Set("Cliente/src/Cliente.c", "Cliente/.gitignore", "Servidor/src/Server.c", "Servidor/.gitignore", ".gitignore", "makefile", "README.md", ".git")
    val absExpRoutes: Set[FileRoute] = expectedRoutes map {absDir + _}

    def doAssertions = {
      val res = results.head._2
      assert(res == Success(absExpRoutes), "DIFF" + res.get.filterNot(absExpRoutes.contains(_)))
      assert(res != Success("anotherThing"))
      assert(res.get.contains(absDir + ".gitignore"))
    }

    initStream.subscribe(onNext = {res => results += res}, onError = {throw _}, onCompleted = {() => waiter(doAssertions); waiter.dismiss})

    main.send(sys, System.Start(Set(absDir)))

    waiter.await

    main.send(sys, System.Shutdown)
  }

}
