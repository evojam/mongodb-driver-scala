package com.evojam.mongodb.client.model.operation

import java.util.concurrent.TimeUnit

import scala.language.implicitConversions

import com.mongodb.MongoNamespace
import com.mongodb.client.model.CountOptions
import com.mongodb.operation.{CountOperation => MongoCountOperation}
import org.bson.codecs.configuration.CodecRegistry
import org.bson.{BsonDocument, BsonString, BsonValue}

import com.evojam.mongodb.client.util.BsonUtil

case class CountOperation(
  namespace: MongoNamespace,
  filter: Option[BsonDocument] = None,
  hint: Option[BsonValue] = None,
  skip: Option[Long] = None,
  limit: Option[Long] = None,
  maxTimeMS: Option[Long] = None) {

  require(namespace != null, "namespace cannot be null")
}

object CountOperation {
  def exception(field: String) =
    throw new IllegalStateException(s"$field cannot be empty")

  implicit def countOperation2Mongo[T](to: CountOperation) =
    new MongoCountOperation(to.namespace)
      .filter(to.filter.getOrElse(exception("filter")))
      .skip(to.skip.getOrElse(exception("skip")))
      .limit(to.limit.getOrElse(exception("limit")))
      .maxTime(to.maxTimeMS.getOrElse(exception("maxTimeMS")), TimeUnit.MILLISECONDS)
      .hint(to.hint.getOrElse(null))

  def apply(namespace: MongoNamespace, filter: BsonDocument, opts: CountOptions)
    (implicit documentClass: Class[_], codecRegistry: CodecRegistry): CountOperation =
    CountOperation(
      namespace,
      Some(filter),
      CountOperation.getHint(opts),
      Some(opts.getSkip),
      Some(opts.getLimit),
      Some(opts.getMaxTime(TimeUnit.MILLISECONDS)))

  def getHint(opts: CountOptions)(implicit docClass: Class[_], codecReg: CodecRegistry) =
    if (opts.getHint != null) {
      Some(BsonUtil.toBsonDocument(opts.getHint))
    } else if (opts.getHintString != null) {
      Some(new BsonString(opts.getHintString))
    } else {
      None
    }
}
