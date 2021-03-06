package com.evojam.mongodb.client.cursor

import java.util.concurrent.TimeUnit.MILLISECONDS

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.TimeUnit

import com.mongodb.ReadPreference
import com.mongodb.operation.ListDatabasesOperation
import org.bson.codecs.Codec

import com.evojam.mongodb.client.ObservableOperationExecutor

private[client] case class ListDatabasesCursor(
  readPreference: ReadPreference,
  executor: ObservableOperationExecutor,
  maxTimeMS: Long = 0) extends Cursor {

  require(readPreference != null, "readPreference cannot be null")
  require(executor != null, "executor cannot be null")

  override protected def rawHead[R: Codec]()(implicit exc: ExecutionContext) =
    cursor().head()

  override protected def rawForeach[R: Codec](f: R => Unit)(implicit exc: ExecutionContext) =
    cursor().foreach(f)

  override protected def rawObservable[R: Codec]()(implicit exc: ExecutionContext) =
    cursor().observable()

  override protected def rawObservable[R: Codec](batchSize: Int)(implicit exc: ExecutionContext) =
    cursor().observable(batchSize)

  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListDatabasesCursor =
    this.copy(maxTimeMS = MILLISECONDS.convert(maxTime, timeUnit))

  private def cursor[R: Codec]() =
    OperationCursor(listDatabasesOperation, readPreference, executor)

  private def listDatabasesOperation[R]()(implicit c: Codec[R]): ListDatabasesOperation[R] =
    new ListDatabasesOperation[R](c).maxTime(maxTimeMS, MILLISECONDS)
}
