package com.evojam.mongodb.client.iterable

import com.mongodb.ReadPreference
import org.bson.codecs.Codec
import org.bson.codecs.Encoder

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.operation.ListCollectionOperation

case class ListCollectionsIterable[T: Encoder](
  dbName: String,
  readPreference: ReadPreference,
  executor: ObservableOperationExecutor,
  filter: Option[T] = None,
  maxTime: Option[Long] = None,
  batchSize: Option[Int] = None) extends MongoIterable {

  def filter(filter: T): ListCollectionsIterable[T] = {
    require(filter != null, "filter cannot be null")
    this.copy(filter = Some(filter))
  }

  def maxTime(time: Long): ListCollectionsIterable[T] = {
    require(time >= 0L, "time cannot be negative")
    this.copy(maxTime = Some(time))
  }

  def batchSize(size: Int): ListCollectionsIterable[T] = {
    require(size >= 0, "size cannot be negative")
    this.copy(batchSize = Some(size))
  }

  private def execute[R: Codec]: OperationIterable[R] =
    execute(queryOperation[R])

  private def execute[R: Codec](lco: ListCollectionOperation[T, R]): OperationIterable[R] =
    OperationIterable(lco, readPreference, executor)

  private def queryOperation[R]()(implicit c: Codec[R]) =
    ListCollectionOperation[T, R](dbName, c, filter, batchSize, maxTime)

  override def head[R: Codec] =
    execute(queryOperation.copy(batchSize = Some(-1))).head

  override def headOpt[R: Codec] =
    execute(queryOperation.copy(batchSize = Some(-1))).headOpt

  override def foreach[R: Codec](f: R => Unit) =
    execute.foreach(f)

  override def cursor[R: Codec](batchSize: Option[Int]) =
    execute.cursor(batchSize)

  override def collect[R: Codec]() = execute.collect()
}
