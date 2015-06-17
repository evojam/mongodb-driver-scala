package com.evojam.mongodb.client.cursor

import com.mongodb.ReadPreference
import org.bson.codecs.Codec
import org.bson.codecs.Encoder

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.operation.ListCollectionOperation

private[client] case class ListCollectionsCursor[T: Encoder](
  dbName: String,
  readPreference: ReadPreference,
  executor: ObservableOperationExecutor,
  filter: Option[T] = None,
  maxTime: Option[Long] = None,
  batchSize: Option[Int] = None) extends Cursor {

  override protected def rawHead[R: Codec]() =
    execute(queryOperation.copy(batchSize = Some(-1))).head

  override protected def rawHeadOpt[R: Codec]() =
    execute(queryOperation.copy(batchSize = Some(-1))).headOpt

  override protected def rawForeach[R: Codec](f: R => Unit) =
    execute.foreach(f)

  override protected def rawObservable[R: Codec]() =
    execute.observable()

  override protected def rawObservable[R: Codec](batchSize: Int) =
    execute.observable(batchSize)

  override protected def rawCollect[R: Codec]() =
    execute.collect()

  def filter(filter: T): ListCollectionsCursor[T] = {
    require(filter != null, "filter cannot be null")
    this.copy(filter = Some(filter))
  }

  def maxTime(time: Long): ListCollectionsCursor[T] = {
    require(time >= 0L, "time cannot be negative")
    this.copy(maxTime = Some(time))
  }

  def batchSize(size: Int): ListCollectionsCursor[T] = {
    require(size >= 0, "size cannot be negative")
    this.copy(batchSize = Some(size))
  }

  private def execute[R: Codec]: OperationCursor[R] =
    execute(queryOperation[R])

  private def execute[R: Codec](lco: ListCollectionOperation[T, R]): OperationCursor[R] =
    OperationCursor(lco, readPreference, executor)

  private def queryOperation[R]()(implicit c: Codec[R]) =
    ListCollectionOperation[T, R](dbName, c, filter, batchSize, maxTime)
}
