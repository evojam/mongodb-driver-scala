package com.evojam.mongodb.client.model.operation

import java.util.concurrent.TimeUnit

import scala.language.implicitConversions

import com.mongodb.operation.{FindOperation => MongoFindOperation}
import com.mongodb.{CursorType, MongoNamespace}
import org.bson.codecs.Codec
import org.bson.codecs.Decoder
import org.bson.codecs.Encoder

import com.evojam.mongodb.client.util.BsonUtil

case class FindOperation[T, R](
  namespace: MongoNamespace,
  decoder: Decoder[R],
  filter: Option[T],
  batchSize: Int,
  limit: Int,
  modifiers: Option[T],
  projection: Option[T],
  skip: Int,
  sort: Option[T],
  slaveOk: Boolean,
  oplogRelay: Boolean,
  noCursorTimeout: Boolean,
  partial: Boolean,
  maxTime: Long,
  maxTimeUnit: TimeUnit = TimeUnit.MILLISECONDS,
  cursorType: CursorType = CursorType.NonTailable) {

  require(maxTimeUnit != null, "maxTimeUnit cannot be null")
  require(cursorType != null, "cursorType cannot be null")
}

object FindOperation {
  implicit def findOperation2Mongo[T: Encoder, R](fo: FindOperation[T, R]) =
    new MongoFindOperation(fo.namespace, fo.decoder)
      .filter(BsonUtil.toBson(fo.filter))
      .batchSize(fo.batchSize)
      .limit(fo.limit)
      .modifiers(BsonUtil.toBson(fo.modifiers))
      .projection(BsonUtil.toBson(fo.projection))
      .skip(fo.skip)
      .sort(BsonUtil.toBson(fo.sort))
      .slaveOk(fo.slaveOk)
      .oplogReplay(fo.oplogRelay)
      .noCursorTimeout(fo.noCursorTimeout)
      .partial(fo.partial)
      .maxTime(fo.maxTime, fo.maxTimeUnit)
      .cursorType(fo.cursorType)
}
