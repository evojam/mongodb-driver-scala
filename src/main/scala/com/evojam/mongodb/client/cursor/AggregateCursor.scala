package com.evojam.mongodb.client.cursor

import scala.concurrent.{ExecutionContext, Future}

import com.mongodb.{MongoNamespace, ReadPreference}
import org.bson.{BsonDocument, BsonValue}
import org.bson.codecs.{Codec, Encoder}

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.util.BsonUtil
import com.evojam.mongodb.client.model.operation.AggregateOperation

private[client] case class AggregateCursor[T: Encoder](
  pipeline: List[T],
  namespace: MongoNamespace,
  readPreference: ReadPreference,
  executor: ObservableOperationExecutor,
  allowDiskUse: Option[Boolean] = None,
  batchSize: Option[Int] = None,
  useCursor: Option[Boolean] = None,
  maxTimeMS: Option[Long] = None) extends Cursor {

  require(pipeline != null, "pipeline cannot be null")
  require(!pipeline.isEmpty, "pipeline cannot be empty")
  require(namespace != null, "namespace cannot be null")
  require(readPreference != null, "readPreference cannot be null")
  require(executor != null, "executor cannot be null")
  require(allowDiskUse != null, "allowDiskUse cannot be null")
  require(batchSize != null, "batchSize cannot be null")
  require(useCursor != null, "useCursor cannot be null")
  require(maxTimeMS != null, "maxTimeMS cannot be null")

  override protected def rawHead[R: Codec]()(implicit exc: ExecutionContext) =
    cursor().head()

  override protected def rawForeach[R: Codec](f: R => Unit)(implicit exc: ExecutionContext) =
    cursor().foreach(f)

  override protected def rawObservable[R: Codec]()(implicit exc: ExecutionContext) =
    cursor().observable()

  override protected def rawObservable[R: Codec](batchSize: Int)(implicit exc: ExecutionContext) =
    cursor(aggregateOperation[R](bsonPipeline).copy(batchSize = Some(batchSize)))
      .observable(batchSize)

  def toCollection(): Future[Unit] = ???

  private def cursor[R: Codec]()(implicit ec: ExecutionContext): OperationCursor[R] =
    cursor(aggregateOperation[R](bsonPipeline))

  private def cursor[R: Codec](operation: AggregateOperation[R]): OperationCursor[R] =
    OperationCursor(operation, readPreference, executor)

  private def aggregateOperation[R](bsonPipeline: List[BsonDocument])(implicit c: Codec[R]) =
    AggregateOperation[R](namespace, bsonPipeline, c, maxTimeMS, allowDiskUse, batchSize, useCursor)

  private def outCollectionNamespace(outCollection: BsonValue): MongoNamespace =
    new MongoNamespace(
      namespace.getDatabaseName,
      outCollection.asString().getValue)

  private def aggregateOutCollection(bsonPipeline: List[BsonDocument]): Option[BsonValue] =
    bsonPipeline.lastOption.flatMap(last => Option(last.get("$out")))

  private def bsonPipeline() =
    pipeline.map(BsonUtil.toBson(_))
}
