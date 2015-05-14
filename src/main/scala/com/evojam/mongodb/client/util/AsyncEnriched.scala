package com.evojam.mongodb.client.util

import scala.collection.JavaConversions._

import com.mongodb.async.{ AsyncBatchCursor, SingleResultCallback }
import rx.lang.scala.Observable
import rx.lang.scala.subjects.PublishSubject

trait AsyncEnriched {

  implicit class AsyncBatchCursorEnriched[T](wrapped: AsyncBatchCursor[T]) {

    def takeFirstAsObservable = {
      wrapped.setBatchSize(1)

      val observable = asObservable

      observable.doOnNext(_ => wrapped.close())
      observable.take(1)
    }

    def asObservable: Observable[T] = {

      val subject = PublishSubject[T]()

      wrapped.next(new SingleResultCallback[java.util.List[T]] {
        override def onResult(result: java.util.List[T], t: Throwable) = {
          if(t == null) {
            Option(result)
              .map(_.foreach(subject.onNext)) // FIXME: Better map to Unit than patternmatch
              .getOrElse(subject.onCompleted())
          } else {
            subject.onError(t)
          }
        }
      })

      subject
    }
  }

}
