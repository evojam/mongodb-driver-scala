package com.evojam.mongodb.client.model

import java.util.concurrent.TimeUnit

import scala.language.implicitConversions

import com.mongodb.MongoNamespace
import com.mongodb.operation.{ DistinctOperation => MongoDistinctOperation }
import org.bson.codecs.Decoder
import org.bson.codecs.Encoder

import com.evojam.mongodb.client.util.BsonUtil

case class DistinctOperation[T, R](
  namespace: MongoNamespace,
  fieldName: String,
  decoder: Decoder[R],
  filter: Option[T] = None,
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
  implicit def distinctOperation2MongoOperation[T: Encoder, R](operation: DistinctOperation[T, R]) =
    new MongoDistinctOperation(operation.namespace, operation.fieldName, operation.decoder)
      .filter(BsonUtil.toBson[T](operation.filter))
      .maxTime(operation.maxTime.getOrElse(0), operation.timeUnit)
}
