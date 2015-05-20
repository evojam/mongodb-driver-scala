package com.evojam.mongodb.client.model.operation

import java.util.concurrent.TimeUnit

import scala.language.{implicitConversions, postfixOps}

import com.mongodb.MongoNamespace
import com.mongodb.operation.{ListIndexesOperation => MongoListIndexesOperation}
import org.bson.codecs.Decoder

case class ListIndexesOperation[T](
  namespace: MongoNamespace,
  decoder: Decoder[T],
  batchSize: Int,
  maxTimeMS: Long) {

  require(namespace != null, "namespace cannot be null")
  require(decoder != null, "decoder cannot be null")
  require(batchSize >= 0, "batchSize cannot be negative")
  require(maxTimeMS >= 0, "maxTimeMS cannot be negative")
}

object ListIndexesOperation {
  implicit def listIndexesOperation2Mongo[T](lio: ListIndexesOperation[T]): MongoListIndexesOperation[T] =
    new MongoListIndexesOperation[T](lio.namespace, lio.decoder)
      .batchSize(lio.batchSize)
      .maxTime(lio.maxTimeMS, TimeUnit.MILLISECONDS)
}
