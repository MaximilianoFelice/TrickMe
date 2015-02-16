package TrickMe
package Internals

import TrickMe.PreProcessing.HashFilter._
import akka.pattern.ask

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import scala.util.Try

/**
 * Created by maximilianofelice on 11/02/15.
 */
object Utils {

  def getFilesRecursively(dir: java.io.File): Set[String] = {
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

  def mkabsolute(path: FileRoute): FileRoute = (new java.io.File(path)).getAbsolutePath
}
