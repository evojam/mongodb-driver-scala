package com.evojam.mongodb.client.cursor

import java.util.concurrent.TimeUnit.MILLISECONDS

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

  override protected def rawHead[R: Codec]() =
    cursor().head()

  override protected def rawHeadOpt[R: Codec]() =
    cursor().headOpt()

  override protected def rawForeach[R: Codec](f: R => Unit) =
    cursor().foreach(f)

  override protected def rawObservable[R: Codec]() =
    cursor().observable()

  override protected def rawObservable[R: Codec](batchSize: Int) =
    cursor().observable(batchSize)

  override protected def rawCollect[R: Codec]() =
    cursor().collect()

  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListDatabasesCursor =
    this.copy(maxTimeMS = MILLISECONDS.convert(maxTime, timeUnit))

  private def cursor[R: Codec]() =
    OperationCursor(listDatabasesOperation, readPreference, executor)

  private def listDatabasesOperation[R]()(implicit c: Codec[R]): ListDatabasesOperation[R] =
    new ListDatabasesOperation[R](c).maxTime(maxTimeMS, MILLISECONDS)
}
