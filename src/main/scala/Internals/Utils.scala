package TrickMe
package Internals

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

}
