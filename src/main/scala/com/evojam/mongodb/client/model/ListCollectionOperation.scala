package com.evojam.mongodb.client.model

import scala.language.implicitConversions

import java.util.concurrent.TimeUnit.MILLISECONDS

import org.bson.BsonDocument
import org.bson.codecs.Decoder

import com.mongodb.operation.{ ListCollectionsOperation => MongoListCollectionsOperation }

case class ListCollectionOperation[T](
  dbName: String,
  decoder: Decoder[T],
  filter: BsonDocument,
  batchSize: Int,
  maxTimeMS: Long) {

  require(dbName != null, "dbName cannot be null")
  require(decoder != null, "decoder cannot be null")
  require(filter != null, "filter cannot be null")
  require(batchSize > 0, "batchSize cannot be negative")
  require(maxTimeMS > 0, "maxTimeMS cannot be negative")
}

object ListCollectionOperation {
  implicit def listOperationToMongo[T](lco: ListCollectionOperation[T]): MongoListCollectionsOperation[T] =
    new MongoListCollectionsOperation(lco.dbName, lco.decoder)
      .filter(lco.filter)
      .batchSize(lco.batchSize)
      .maxTime(lco.maxTimeMS, MILLISECONDS)
}
