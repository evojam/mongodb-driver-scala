package com.evojam.mongodb.client.util

import scala.collection.JavaConversions._

import com.mongodb.async.{ AsyncBatchCursor, SingleResultCallback }
import rx.lang.scala.Observable
import rx.lang.scala.Subscriber

trait AsyncEnriched {

  implicit class AsyncBatchCursorEnriched[T](wrapped: AsyncBatchCursor[T]) {

    def takeFirstAsObservable = {
      wrapped.setBatchSize(1)

      val observable = asObservable

      observable.doOnNext(_ => wrapped.close())
      observable.take(1)
    }

    def asObservable: Observable[T] =
      Observable[T](subscriber =>
        wrapped.next(onNextCallback(subscriber, _.foreach(subscriber.onNext))))

    def asBatchObservable(batchSize: Int): Observable[List[T]] = {
      wrapped.setBatchSize(batchSize)
      Observable[List[T]](subscriber =>
        wrapped.next(onNextCallback(subscriber, chunk => subscriber.onNext(chunk.toList))))
    }

    private def onNextCallback[R](
      subscriber: Subscriber[R],
      f: java.util.List[T] => Unit): SingleResultCallback[java.util.List[T]] =
      new SingleResultCallback[java.util.List[T]] {
        override def onResult(result: java.util.List[T], t: Throwable) =
          if (t == null) {
            Option(result).foreach(f)
            if (!wrapped.isClosed()) {
              wrapped.next(onNextCallback(subscriber, f))
            } else {
              subscriber.onCompleted()
            }
          } else {
            subscriber.onError(t)
            wrapped.close()
          }
      }
  }
}
