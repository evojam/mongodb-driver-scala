package com.evojam.mongodb.client.model.result

import scala.collection.JavaConversions._
import scala.language.implicitConversions

import com.mongodb.bulk.{BulkWriteUpsert, BulkWriteResult => MongoBulkWriteResult}

case class BulkWriteResult(
  acknowledged: Boolean,
  insertedCount: Option[Int],
  matchedCount: Option[Int],
  removedCount: Option[Int],
  modifiedCount: Option[Int],
  upserts: List[BulkWriteUpsert]) {

  require(insertedCount != null, "insertedCount cannot be null")
  require(matchedCount != null, "matchedCount cannot be null")
  require(removedCount != null, "removedCount cannot be null")
  require(modifiedCount != null, "modifiedCount cannot be null")
  require(upserts != null, "upserts cannot be null")
  require(acknowledged == insertedCount.isDefined, "insertedCount must be defined when acknowledged")
  require(acknowledged == matchedCount.isDefined, "matchedCount must be defined when acknowledged")
  require(acknowledged == removedCount.isDefined, "removedCount must be defined when acknowledged")
}

object BulkWriteResult {
  implicit def bulkWriteResult2Mongo(result: BulkWriteResult): MongoBulkWriteResult =
    new MongoBulkWriteResult {
      override def getDeletedCount =
        result.removedCount.getOrElse(unacknowledgedException())

      override def getInsertedCount() =
        result.insertedCount.getOrElse(unacknowledgedException())

      override def getMatchedCount() =
        result.matchedCount.getOrElse(unacknowledgedException())

      override def isModifiedCountAvailable() =
        result.modifiedCount.isDefined

      override def wasAcknowledged() =
        result.acknowledged

      override def getUpserts() =
        result.upserts

      override def getModifiedCount: Int =
        result.modifiedCount.getOrElse(throw new IllegalStateException("Modified count is not defined."))

      private def unacknowledgedException() =
        throw new UnsupportedOperationException("Cannot get information about unacknowledged write.")
    }

  implicit def mongo2BulkWriteResult(result: MongoBulkWriteResult): BulkWriteResult = result.wasAcknowledged() match {
    case true => BulkWriteResult(
      true,
      Some(result.getInsertedCount),
      Some(result.getMatchedCount),
      Some(result.getDeletedCount),
      Option(result.isModifiedCountAvailable)
        .map(_ => Some(result.getModifiedCount))
        .getOrElse(None),
      result.getUpserts.toList)
    case false => BulkWriteResult(false, None, None, None, None, List())
  }
}
