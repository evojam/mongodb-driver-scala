package com.evojam.mongodb.client.iterable

// TODO: Think about execution context we want to use

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.existentials

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.util.AsyncEnriched
import com.mongodb.ReadPreference
import com.mongodb.async.AsyncBatchCursor
import com.mongodb.operation.AsyncReadOperation

case class OperationIterable[T](operation: AsyncReadOperation[_ <: AsyncBatchCursor[T]], readPreference: ReadPreference,
  executor: ObservableOperationExecutor) extends MongoIterable[T] with AsyncEnriched {

  private lazy val observable = executor.executeAsync(operation, readPreference).flatMap(_.asObservable)

  override def head = headOpt.map(_.get)

  override def cursor(batchSize: Option[Int]) = observable

  override def headOpt: Future[Option[T]] =
    executor.executeAsync(operation, readPreference)
      .flatMap(_.takeFirstAsObservable)
      .first
      .toList
      .map(_.headOption).toBlocking.toFuture

  override def foreach(f: T => Unit) = observable.foreach(f)

  override def map[U](f: T => U) = ResultIterable(observable.map(f))

  override def collect() = observable.toList.toBlocking.toFuture
}
