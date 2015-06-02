package com.evojam.mongodb.client.iterable

import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import org.bson.codecs.Codec

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.operation.ListIndexesOperation

case class ListIndexesIterable(
  namespace: MongoNamespace,
  readPreference: ReadPreference,
  maxTime: Long = 0L,
  batchSize: Int = 0,
  executor: ObservableOperationExecutor) extends MongoIterable {

  override def head[R: Codec] =
    execute[R](queryOperation.copy(batchSize = -1)).head

  override def headOpt[R: Codec] =
    execute(queryOperation.copy(batchSize = -1)).headOpt

  override def foreach[R: Codec](f: R => Unit) =
    execute.foreach(f)

  override def cursor[R: Codec](batchSize: Option[Int]) =
    execute.cursor(batchSize)

  override def collect[R: Codec]() =
    execute.collect

  private def execute[R: Codec]: OperationIterable[R] =
    execute(queryOperation[R])

  private def execute[R: Codec](lio: ListIndexesOperation[R]): OperationIterable[R] =
    OperationIterable(lio, readPreference, executor)

  private def queryOperation[R]()(implicit c: Codec[R]): ListIndexesOperation[R] =
    ListIndexesOperation[R](namespace, c, batchSize, maxTime)
}
