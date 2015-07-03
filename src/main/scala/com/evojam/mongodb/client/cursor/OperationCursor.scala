package com.evojam.mongodb.client.cursor

import scala.concurrent.ExecutionContext
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

  require(operation != null, "operation cannot be null")
  require(readPreference != null, "readPreference cannot be null")
  require(executor != null, "executor cannot be null")

  def head()(implicit exc: ExecutionContext): Observable[R] =
    executor.executeAsync(operation, readPreference)
      .flatMap(_.takeFirstAsObservable)
      .first

  def foreach(f: R => Unit)(implicit exc: ExecutionContext): Unit =
    execute().foreach(f)

  def observable()(implicit exc: ExecutionContext): Observable[R] =
    execute()

  def observable(batchSize: Int)(implicit exc: ExecutionContext): Observable[List[R]] =
    executor.executeAsync(operation, readPreference)
      .flatMap(_.asBatchObservable(batchSize))

  private def execute()(implicit exc: ExecutionContext): Observable[R] =
    executor.executeAsync(operation, readPreference)
      .flatMap(_.asObservable)
}
