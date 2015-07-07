package com.evojam.mongodb.client.model.result

import scala.collection.JavaConversions._
import scala.language.implicitConversions

import com.mongodb.bulk.{BulkWriteResult => MongoBulkWriteResult}
import com.mongodb.client.result.{UpdateResult => MongoUpdateResult}
import org.bson.codecs.Codec

import com.evojam.mongodb.client.util.BsonUtil

abstract class UpdateResult[T: Codec](
  val acknowledged: Boolean,
  val matched: Option[Long],
  val modified: Option[Long],
  val upsertId: Option[T]) {

  require(upsertId != null, "upsertId cannot be null")
}

object UpdateResult {
  implicit def updateResult2Mongo[T: Codec](
    result: UpdateResult[T]): MongoUpdateResult =
    result match {
      case AcknowledgedUpdateResult(matched, modified, upsertId) =>
        MongoUpdateResult.acknowledged(
          matched, modified, BsonUtil.toBson(upsertId))
      case _ =>
        MongoUpdateResult.unacknowledged()
    }

  implicit def mongoUpdateResult2UpdateResult[T: Codec](
    mongoResult: MongoUpdateResult): UpdateResult[T] = mongoResult.wasAcknowledged match {
    case true => AcknowledgedUpdateResult[T](
      mongoResult.getMatchedCount,
      mongoResult.getModifiedCount,
      Option(mongoResult.getUpsertedId)
        .flatMap(id => BsonUtil.fromBsonValue(id)))
    case false => UnacknowledgedUpdateResult()
  }

  implicit def bulkWriteResult2UpdateResult[T: Codec](
    result: MongoBulkWriteResult): UpdateResult[T] = result.wasAcknowledged match {
    case true => AcknowledgedUpdateResult(
      result.getMatchedCount,
      result.getModifiedCount,
      result.getUpserts
        .headOption
        .map(_.getId)
        .flatMap(id => BsonUtil.fromBsonValue(id)))
    case false => UnacknowledgedUpdateResult()
  }

  private case class AcknowledgedUpdateResult[T: Codec](
    matchedCount: Long,
    modifiedCount: Long,
    upsertIdDoc: Option[T]) extends UpdateResult[T](true, Some(matchedCount), Some(modifiedCount), upsertIdDoc)

  private case class UnacknowledgedUpdateResult[T: Codec]() extends UpdateResult[T](false, None, None, None)
}
