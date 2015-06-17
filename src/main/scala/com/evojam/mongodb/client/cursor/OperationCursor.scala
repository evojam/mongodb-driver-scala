package com.evojam.mongodb.client.cursor

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

private[client] case class OperationCursor[R: Codec](
  operation: AsyncReadOperation[_ <: AsyncBatchCursor[R]],
  readPreference: ReadPreference,
  executor: ObservableOperationExecutor) extends AsyncEnriched {

  private lazy val executedObservable =
    executor.executeAsync(operation, readPreference).flatMap(_.asObservable)

  def head: Future[R] = headOpt.map(_.get)

  def headOpt: Future[Option[R]] =
    executor.executeAsync(operation, readPreference)
      .flatMap(_.takeFirstAsObservable)
      .first
      .toList
      .map(_.headOption).toBlocking.toFuture

  def foreach(f: R => Unit): Unit = executedObservable.foreach(f)

  def observable(): Observable[R] = executedObservable

  def observable(batchSize: Int): Observable[List[R]] =
    executor.executeAsync(operation, readPreference)
      .flatMap(_.asBatchObservable(batchSize))

  def collect(): Future[List[R]] = executedObservable.toList.toBlocking.toFuture
}
