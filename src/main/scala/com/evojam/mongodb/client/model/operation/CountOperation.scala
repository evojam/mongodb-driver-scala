package com.evojam.mongodb.client.model.operation

import java.util.concurrent.TimeUnit

import scala.language.implicitConversions

import com.mongodb.MongoNamespace
import com.mongodb.client.model.CountOptions
import com.mongodb.operation.{ CountOperation => MongoCountOperation }
import org.bson.codecs.Codec
import org.bson.codecs.Encoder

import com.evojam.mongodb.client.util.BsonUtil

case class CountOperation[T](
  namespace: MongoNamespace,
  filter: Option[T] = None,
  hint: Option[T] = None,
  skip: Option[Long] = None,
  limit: Option[Long] = None,
  maxTimeMS: Option[Long] = None) {

  require(namespace != null, "namespace cannot be null")
}

object CountOperation {
  def exception(field: String) =
    throw new IllegalStateException(s"$field cannot be empty")

  implicit def countOperation2Mongo[T: Encoder](to: CountOperation[T]) =
    new MongoCountOperation(to.namespace)
      .filter(to.filter.map(BsonUtil.toBson(_)).getOrElse(exception("filter")))
      .skip(to.skip.getOrElse(exception("skip")))
      .limit(to.limit.getOrElse(exception("limit")))
      .maxTime(to.maxTimeMS.getOrElse(exception("maxTimeMS")), TimeUnit.MILLISECONDS)
      .hint(to.hint.map(BsonUtil.toBson(_)).getOrElse(null))

  def apply[T: Codec](namespace: MongoNamespace, filter: Option[T], opts: CountOptions): CountOperation[T] =
    CountOperation(
      namespace,
      filter,
      BsonUtil.fromBson(opts.getHint()),
      Some(opts.getSkip),
      Some(opts.getLimit),
      Some(opts.getMaxTime(TimeUnit.MILLISECONDS)))
}
