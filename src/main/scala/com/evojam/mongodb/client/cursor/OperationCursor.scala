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

  def head(): Future[R] =
    headOpt().map(_.get)

  def headOpt(): Future[Option[R]] =
    executor.executeAsync(operation, readPreference)
      .flatMap(_.takeFirstAsObservable)
      .first
      .toList
      .map(_.headOption).toBlocking.toFuture

  def foreach(f: R => Unit): Unit =
    execute().foreach(f)

  def observable(): Observable[R] =
    execute()

  def observable(batchSize: Int): Observable[List[R]] =
    executor.executeAsync(operation, readPreference)
      .flatMap(_.asBatchObservable(batchSize))

  def collect(): Future[List[R]] =
    execute().toList.toBlocking.toFuture

  private def execute(): Observable[R] =
    executor.executeAsync(operation, readPreference)
      .flatMap(_.asObservable)
}
