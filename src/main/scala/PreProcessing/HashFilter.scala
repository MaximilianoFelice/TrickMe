package TrickMe
package PreProcessing

import TrickMe.Internals.Utils._
import akka.util.Timeout

import scala.concurrent.duration._

/**
 * Created by maximilianofelice on 11/02/15.
 */

/**
 *  Generates a new stream, based on [[Internals.Starter]] initStream, which filters its results
 *  based on a MD5 Hash file comparison.
 */
object HashFilter extends TrickMeResultPublisher[Set[FileRoute]]{

  implicit val timeout = Timeout(5000 seconds)
  lazy val toFilter: Set[String] = getConfigValue("filteredPaths").get

}

