package com.evojam.mongodb.client.model

import java.util.concurrent.TimeUnit

import scala.language.implicitConversions

import com.mongodb.MongoNamespace
import com.mongodb.operation.{DistinctOperation => MongoDistinctOperation}
import org.bson.BsonDocument
import org.bson.codecs.Decoder

case class DistinctOperation[T](
  namespace: MongoNamespace,
  fieldName: String,
  decoder: Decoder[T],
  filter: Option[BsonDocument] = None,
  maxTime: Option[Long] = None,
  timeUnit: TimeUnit = TimeUnit.MILLISECONDS) {

  require(namespace != null, "namespace cannot be null")
  require(fieldName != null, "fieldName cannot be null")
  require(decoder != null, "decoder cannot be null")
  require(filter != null, "filter cannot be null")
  require(maxTime != null, "maxTimeMS cannot be null")
  require(timeUnit != null, "timeUnit cannot be null")
}

object DistinctOperation {
  implicit def distinctOperation2MongoOperation[T](operation: DistinctOperation[T]) =
    new MongoDistinctOperation(operation.namespace, operation.fieldName, operation.decoder)
      .filter(operation.filter.getOrElse(null))
      .maxTime(operation.maxTime.getOrElse(0), operation.timeUnit)
}
