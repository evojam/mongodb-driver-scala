package com.evojam.mongodb.client.iterable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.existentials

import com.mongodb.ReadPreference
import com.mongodb.async.AsyncBatchCursor
import com.mongodb.operation.AsyncReadOperation
import org.bson.codecs.Codec
import rx.lang.scala.Observable

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.util.AsyncEnriched

private[client] case class OperationIterable[R: Codec](
  operation: AsyncReadOperation[_ <: AsyncBatchCursor[R]],
  readPreference: ReadPreference,
  executor: ObservableOperationExecutor) extends AsyncEnriched {

  private lazy val observable = executor.executeAsync(operation, readPreference).flatMap(_.asObservable)

  def head: Future[R] = headOpt.map(_.get)

  def headOpt: Future[Option[R]] =
    executor.executeAsync(operation, readPreference)
      .flatMap(_.takeFirstAsObservable)
      .first
      .toList
      .map(_.headOption).toBlocking.toFuture

  def foreach(f: R => Unit): Unit = observable.foreach(f)

  def cursor(): Observable[R] = observable

  def cursor(batchSize: Int): Observable[List[R]] =
    executor.executeAsync(operation, readPreference)
      .flatMap(_.asBatchObservable(batchSize))

  def collect(): Future[List[R]] = observable.toList.toBlocking.toFuture
}
