package com.evojam.mongodb.client.iterable

import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import org.bson.codecs.Codec

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.operation.ListIndexesOperation

private[client] case class ListIndexesIterable(
  namespace: MongoNamespace,
  readPreference: ReadPreference,
  maxTime: Long = 0L,
  batchSize: Int = 0,
  executor: ObservableOperationExecutor) extends MongoIterable {

  override protected def rawHead[R: Codec]() =
    execute[R](queryOperation.copy(batchSize = -1)).head

  override protected def rawHeadOpt[R: Codec]() =
    execute(queryOperation.copy(batchSize = -1)).headOpt

  override protected def rawForeach[R: Codec](f: R => Unit) =
    execute.foreach(f)

  override protected def rawCursor[R: Codec]() =
    execute.cursor()

  override protected def rawCursor[R: Codec](batchSize: Int) =
    execute.cursor(batchSize)

  override protected def rawCollect[R: Codec]() =
    execute.collect

  private def execute[R: Codec]: OperationIterable[R] =
    execute(queryOperation[R])

  private def execute[R: Codec](lio: ListIndexesOperation[R]): OperationIterable[R] =
    OperationIterable(lio, readPreference, executor)

  private def queryOperation[R]()(implicit c: Codec[R]): ListIndexesOperation[R] =
    ListIndexesOperation[R](namespace, c, batchSize, maxTime)
}
