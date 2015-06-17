package com.evojam.mongodb.client.cursor

import java.util.concurrent.TimeUnit.MILLISECONDS

import scala.concurrent.duration.TimeUnit

import com.mongodb.ReadPreference
import com.mongodb.operation.ListDatabasesOperation
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecRegistry

import com.evojam.mongodb.client.ObservableOperationExecutor

private[client] case class ListDatabasesCursor(
  codecRegistry: CodecRegistry,
  readPreference: ReadPreference,
  executor: ObservableOperationExecutor,
  maxTimeMS: Long = 0) extends Cursor {

  override protected def rawHead[T: Codec]() =
    executedOperation.head

  override protected def rawHeadOpt[T: Codec]() =
    executedOperation.headOpt

  override protected def rawForeach[T: Codec](f: T => Unit) =
    executedOperation.foreach(f)

  override protected def rawObservable[T: Codec]() =
    executedOperation.observable()

  override protected def rawObservable[T: Codec](batchSize: Int) =
    executedOperation.observable(batchSize)

  override protected def rawCollect[T: Codec]() =
    executedOperation.collect()

  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListDatabasesCursor =
    this.copy(maxTimeMS = MILLISECONDS.convert(maxTime, timeUnit))

  private def createOperation[T]()(implicit c: Codec[T]): ListDatabasesOperation[T] =
    new ListDatabasesOperation[T](c).maxTime(maxTimeMS, MILLISECONDS)

  private def executedOperation[T: Codec] =
    OperationCursor(createOperation, readPreference, executor)
}
