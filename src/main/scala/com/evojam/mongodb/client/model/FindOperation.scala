package com.evojam.mongodb.client.model

import java.util.concurrent.TimeUnit

import scala.language.implicitConversions

import com.mongodb.CursorType
import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import com.mongodb.operation.{ FindOperation => MongoFindOperation }

import org.bson.BsonDocument
import org.bson.codecs.Decoder
import org.bson.conversions.Bson
import org.bson.codecs.configuration.CodecRegistry

case class FindOperation[T](
  namespace: MongoNamespace,
  decoder: Decoder[T],
  filter: BsonDocument,
  batchSize: Int,
  limit: Int,
  modifiers: BsonDocument,
  projection: BsonDocument,
  skip: Int,
  sort: BsonDocument,
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
  implicit def findOperation2Mongo[T](fo: FindOperation[T]) =
    new MongoFindOperation(fo.namespace, fo.decoder)
      .filter(fo.filter)
      .batchSize(fo.batchSize)
      .limit(fo.limit)
      .modifiers(fo.modifiers)
      .projection(fo.projection)
      .skip(fo.skip)
      .sort(fo.sort)
      .slaveOk(fo.slaveOk)
      .oplogReplay(fo.oplogRelay)
      .noCursorTimeout(fo.noCursorTimeout)
      .partial(fo.partial)
      .maxTime(fo.maxTime, fo.maxTimeUnit)
      .cursorType(fo.cursorType)
}