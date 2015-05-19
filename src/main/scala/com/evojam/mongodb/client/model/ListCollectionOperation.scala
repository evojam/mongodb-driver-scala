package com.evojam.mongodb.client.model

import java.util.concurrent.TimeUnit.MILLISECONDS

import scala.language.implicitConversions

import com.mongodb.operation.{ ListCollectionsOperation => MongoListCollectionsOperation }
import org.bson.BsonDocument
import org.bson.codecs.Decoder

case class ListCollectionOperation[T](
  dbName: String,
  decoder: Decoder[T],
  filter: Option[BsonDocument],
  batchSize: Option[Int],
  maxTimeMS: Option[Long]) {

  require(dbName != null, "dbName cannot be null")
  require(decoder != null, "decoder cannot be null")
  require(filter != null, "filter cannot be null")
  require(batchSize != null, "batchSize cannot be null")
  require(maxTimeMS != null, "maxTimeMS cannot be null")
}

object ListCollectionOperation {
  implicit def listOperationToMongo[T](lco: ListCollectionOperation[T]): MongoListCollectionsOperation[T] =
    new MongoListCollectionsOperation(lco.dbName, lco.decoder)
      .filter(lco.filter.getOrElse(null))
      .batchSize(lco.batchSize.getOrElse(0))
      .maxTime(lco.maxTimeMS.getOrElse(0L), MILLISECONDS)
}
