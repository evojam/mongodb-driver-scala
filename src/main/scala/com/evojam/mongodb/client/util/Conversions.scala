package com.evojam.mongodb.client.util

import scala.language.implicitConversions

import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.bson.BsonValue

object Conversions {
  def modifiedCount(result: BulkWriteResult): Long =
    if (result.isModifiedCountAvailable) {
      result.getModifiedCount
    } else {
      0L
    }

  def upsertedId(result: BulkWriteResult): BsonValue =
    if (result.getUpserts.isEmpty) {
      null
    } else {
      result.getUpserts.get(0).getId
    }

  implicit def bulkWriteResultToUpdateResult(result: BulkWriteResult): UpdateResult =
    if(result.wasAcknowledged) {
      UpdateResult.acknowledged(
        result.getMatchedCount,
        modifiedCount(result),
        upsertedId(result))
    } else {
      UpdateResult.unacknowledged
    }

  implicit def bulkWriteResultToDeleteResult(result: BulkWriteResult): DeleteResult =
    if (result.wasAcknowledged()) {
      DeleteResult.acknowledged(result.getDeletedCount)
    } else {
      DeleteResult.unacknowledged
    }
}
