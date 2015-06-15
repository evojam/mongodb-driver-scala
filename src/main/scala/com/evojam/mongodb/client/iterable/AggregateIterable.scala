package com.evojam.mongodb.client.iterable

import scala.concurrent.Future

import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import org.bson.BsonDocument
import org.bson.BsonValue
import org.bson.codecs.Codec
import org.bson.codecs.Encoder

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.util.BsonUtil
import com.evojam.mongodb.client.model.operation.AggregateOperation

private[client] case class AggregateIterable[T: Encoder](
  pipeline: List[T],
  namespace: MongoNamespace,
  readPreference: ReadPreference,
  executor: ObservableOperationExecutor,
  allowDiskUse: Option[Boolean] = None,
  batchSize: Option[Int] = None,
  useCursor: Option[Boolean] = None,
  maxTimeMS: Option[Long] = None) extends MongoIterable {

  require(pipeline != null, "pipeline cannot be null")
  require(!pipeline.isEmpty, "pipeline cannot be empty")
  require(namespace != null, "namespace cannot be null")
  require(readPreference != null, "readPreference cannot be null")
  require(executor != null, "executor cannot be null")
  require(allowDiskUse != null, "allowDiskUse cannot be null")
  require(batchSize != null, "batchSize cannot be null")
  require(useCursor != null, "useCursor cannot be null")
  require(maxTimeMS != null, "maxTimeMS cannot be null")

  override protected def rawHead[R: Codec]() =
    execute().head

  override protected def rawHeadOpt[R: Codec]() =
    execute().headOpt

  override protected def rawForeach[R: Codec](f: R => Unit) =
    execute().foreach(f)

  override protected def rawCursor[R: Codec]() =
    execute().cursor()

  override protected def rawCursor[R: Codec](batchSize: Int) =
    execute(aggregateOperation[R](bsonPipeline).copy(batchSize = Some(batchSize)))
      .cursor(batchSize)

  override protected def rawCollect[R: Codec]() =
    execute().collect()

  def toCollection(): Future[Unit] = ???

  private def execute[R: Codec](): OperationIterable[R] =
    execute(aggregateOperation[R](bsonPipeline))

  private def execute[R: Codec](operation: AggregateOperation[R]): OperationIterable[R] =
    OperationIterable(operation, readPreference, executor)

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
