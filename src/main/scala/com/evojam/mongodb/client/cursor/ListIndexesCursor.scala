package com.evojam.mongodb.client.cursor

import com.mongodb.{ MongoNamespace, ReadPreference }
import org.bson.codecs.Codec

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.operation.ListIndexesOperation

private[client] case class ListIndexesCursor(
  namespace: MongoNamespace,
  readPreference: ReadPreference,
  maxTime: Long = 0L,
  batchSize: Int = 0,
  executor: ObservableOperationExecutor) extends Cursor {

  require(namespace != null, "namespace cannot be null")
  require(readPreference != null, "readPreference cannot be null")
  require(executor != null, "executor cannot be null")

  override protected def rawHead[R: Codec]() =
    cursor(queryOperation.copy(batchSize = -1))
      .head()

  override protected def rawForeach[R: Codec](f: R => Unit) =
    cursor().foreach(f)

  override protected def rawObservable[R: Codec]() =
    cursor().observable()

  override protected def rawObservable[R: Codec](batchSize: Int) =
    cursor().observable(batchSize)

  private def cursor[R: Codec](): OperationCursor[R] =
    cursor(queryOperation[R])

  private def cursor[R: Codec](lio: ListIndexesOperation[R]): OperationCursor[R] =
    OperationCursor(lio, readPreference, executor)

  private def queryOperation[R]()(implicit c: Codec[R]): ListIndexesOperation[R] =
    ListIndexesOperation[R](namespace, c, batchSize, maxTime)
}
