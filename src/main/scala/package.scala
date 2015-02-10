import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject

import scala.io.Source
import scala.util.Try

/**
 * Created by maximilianofelice on 07/02/15.
 */
package object TrickMe {

  type InitialResult = (String, List[Source])


  /* Retransmit Starter observable */
  lazy val initStream: Observable[Try[InitialResult]] = TrickMe.Internals.Starter.resultStream

  trait TrickMeResultPublisher[T] {

    val resultStream: ReplaySubject[Try[T]] = ReplaySubject[Try[T]]()

    def publish(elem: Try[T]) = resultStream.onNext(elem)
    def completed = resultStream.onCompleted()
    def gotError(throwable: Throwable) = resultStream.onError(throwable)

  }

}
