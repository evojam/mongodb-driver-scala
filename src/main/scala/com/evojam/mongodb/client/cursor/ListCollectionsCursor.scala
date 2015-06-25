package com.evojam.mongodb.client.cursor

import com.mongodb.ReadPreference
import org.bson.codecs.{ Codec, Encoder }

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.operation.ListCollectionOperation

private[client] case class ListCollectionsCursor[T: Encoder](
  dbName: String,
  readPreference: ReadPreference,
  executor: ObservableOperationExecutor,
  filter: Option[T] = None,
  maxTime: Option[Long] = None,
  batchSize: Option[Int] = None) extends Cursor {

  require(dbName != null, "dbName cannot be null")
  require(dbName.nonEmpty, "dbName cannot be empty")
  require(readPreference != null, "readPreference cannot be null")
  require(filter != null, "filter cannot be null")
  require(maxTime != null, "maxTime cannot be null")
  require(batchSize != null, "batchSize cannot be null")

  override protected def rawHead[R: Codec]() =
    cursor(queryOperation.copy(batchSize = Some(-1)))
      .head()

  override protected def rawForeach[R: Codec](f: R => Unit) =
    cursor().foreach(f)

  override protected def rawObservable[R: Codec]() =
    cursor().observable()

  override protected def rawObservable[R: Codec](batchSize: Int) =
    cursor().observable(batchSize)

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

  private def cursor[R: Codec](): OperationCursor[R] =
    cursor(queryOperation[R])

  private def cursor[R: Codec](lco: ListCollectionOperation[T, R]): OperationCursor[R] =
    OperationCursor(lco, readPreference, executor)

  private def queryOperation[R]()(implicit c: Codec[R]) =
    ListCollectionOperation[T, R](dbName, c, filter, batchSize, maxTime)
}
