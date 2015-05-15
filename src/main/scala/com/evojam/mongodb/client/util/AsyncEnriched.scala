package com.evojam.mongodb.client.util

import scala.collection.JavaConversions._

import com.mongodb.async.{ AsyncBatchCursor, SingleResultCallback }
import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject

trait AsyncEnriched {

  implicit class AsyncBatchCursorEnriched[T](wrapped: AsyncBatchCursor[T]) {

    def takeFirstAsObservable = {
      wrapped.setBatchSize(1)

      val observable = asObservable

      observable.doOnNext(_ => wrapped.close())
      observable.take(1)
    }

    def asObservable: Observable[T] = {

      val subject = ReplaySubject[T]()

      // TODO: Try to fire next when subscriber connects
      wrapped.next(new SingleResultCallback[java.util.List[T]] {
        override def onResult(result: java.util.List[T], t: Throwable) = {
          if(t == null) {
            Option(result)
              .foreach(_.foreach(subject.onNext))
            subject.onCompleted()
            wrapped.close()
          } else {
            subject.onError(t)
          }
        }
      })

      subject
    }
  }

}
