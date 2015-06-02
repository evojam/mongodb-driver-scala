package com.evojam.mongodb.client.model.operation

import java.util.concurrent.TimeUnit.MILLISECONDS

import scala.language.implicitConversions

import com.mongodb.operation.{ListCollectionsOperation => MongoListCollectionsOperation}
import org.bson.BsonDocument
import org.bson.codecs.Decoder
import org.bson.codecs.Encoder

import com.evojam.mongodb.client.util.BsonUtil

case class ListCollectionOperation[T, R](
  dbName: String,
  decoder: Decoder[R],
  filter: Option[T],
  batchSize: Option[Int],
  maxTimeMS: Option[Long]) {

  require(dbName != null, "dbName cannot be null")
  require(decoder != null, "decoder cannot be null")
  require(filter != null, "filter cannot be null")
  require(batchSize != null, "batchSize cannot be null")
  require(maxTimeMS != null, "maxTimeMS cannot be null")
}

object ListCollectionOperation {
  implicit def listOperationToMongo[T, R](lco: ListCollectionOperation[T, R])
    (implicit e: Encoder[T]): MongoListCollectionsOperation[R] =
    new MongoListCollectionsOperation(lco.dbName, lco.decoder)
      .filter(lco.filter.map(BsonUtil.toBson(_)).getOrElse(null))
      .batchSize(lco.batchSize.getOrElse(0))
      .maxTime(lco.maxTimeMS.getOrElse(0L), MILLISECONDS)
}
