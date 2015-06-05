package com.evojam.mongodb.client.model.operation

import java.util.concurrent.TimeUnit

import scala.collection.JavaConversions.seqAsJavaList
import scala.language.implicitConversions

import com.mongodb.MongoNamespace
import com.mongodb.operation.{ AggregateOperation => MongoAggregateOperation }

import org.bson.BsonDocument
import org.bson.codecs.Decoder

case class AggregateOperation[T](
  namespace: MongoNamespace,
  bsonPipeline: List[BsonDocument],
  decoder: Decoder[T],
  maxTimeMS: Option[Long],
  allowDiskUse: Option[Boolean],
  batchSize: Option[Int],
  useCursor: Option[Boolean]) {

  require(namespace != null, "namespace cannot be null")
  require(bsonPipeline != null, "bsonPipeline cannot be null")
  require(!bsonPipeline.isEmpty, "bsonPipeline cannot be empty")
  require(decoder != null, "decoder cannot be null")
  require(maxTimeMS != null, "maxTimeMS cannot be null")
  require(allowDiskUse != null, "allowDiskUse cannot be null")
  require(batchSize != null, "batchSize cannot be null")
  require(useCursor != null, "useCursor cannot be null")
}

object AggregateOperation {
  implicit def aggregateOperation2Mongo[T](op: AggregateOperation[T]) = {
    val mongoOp = new MongoAggregateOperation(op.namespace, op.bsonPipeline, op.decoder)
    op.maxTimeMS.foreach(mongoOp.maxTime(_, TimeUnit.MILLISECONDS))
    op.allowDiskUse.foreach(mongoOp.allowDiskUse(_))
    op.batchSize.foreach(mongoOp.batchSize(_))
    op.useCursor.foreach(mongoOp.useCursor(_))
    mongoOp
  }
}
