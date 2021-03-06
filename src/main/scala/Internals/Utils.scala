package TrickMe
package Internals

import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.util.Try

/**
 * Created by maximilianofelice on 11/02/15.
 */
object Utils {

  implicit val timeout = Timeout(5 seconds)

  def getFilesRecursively(dir: java.io.File): Set[String] = {
    if (dir.isFile) return Set(dir.getAbsolutePath)
    val contents = for {
      name <- dir.list.toList
      path = dir.getAbsolutePath + "/" + name
      javaFile = (new java.io.File(path))
      if javaFile.exists
    } yield javaFile

    val (dirs, files) = contents partition {_.isDirectory}

    val res = (files map (_.getAbsolutePath)) :::  (dirs flatMap getFilesRecursively)

    res.toSet
  }

  def getConfigValue[T](value: String): Try[T] = Try{
    val future: Future[Try[T]] = (TrickMeSystem ? Internals.System.Config(value)).mapTo[Try[T]]
    val x = Await.ready(future, Duration.Inf)
    val x1 = x.value.get
    val x2 =  x1.flatten
    x2
  }.flatten

  def mkabsolute(path: FileRoute): FileRoute = (new java.io.File(path.stripPrefix("/"))).getAbsolutePath
}
