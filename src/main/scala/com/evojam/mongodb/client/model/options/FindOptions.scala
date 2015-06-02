package com.evojam.mongodb.client.model.options

import java.util.concurrent.TimeUnit

import scala.language.implicitConversions

import com.mongodb.CursorType
import com.mongodb.client.model.{ FindOptions => MongoFindOptions }
import org.bson.codecs.Codec
import org.bson.codecs.Encoder

import com.evojam.mongodb.client.util.BsonUtil

case class FindOptions[T](
  batchSize: Int,
  limit: Int,
  modifiers: Option[T],
  projection: Option[T],
  skip: Int,
  sort: Option[T],
  noCursorTimeout: Boolean,
  oplogRelay: Boolean,
  partial: Boolean,
  maxTime: Long,
  maxTimeUnit: TimeUnit = TimeUnit.MILLISECONDS,
  cursorType: CursorType = CursorType.NonTailable) {

  require(cursorType != null, "cursorType cannot be null")
  require(maxTimeUnit != null, "maxTimeUnit cannot be null")
}

object FindOptions {
  def apply[T]()(implicit c: Codec[T]): FindOptions[T] = {
    val opts = new MongoFindOptions()
    FindOptions(
      opts.getBatchSize,
      opts.getLimit,
      BsonUtil.fromBson(opts.getModifiers),
      BsonUtil.fromBson(opts.getProjection),
      opts.getSkip,
      BsonUtil.fromBson(opts.getSort),
      opts.isNoCursorTimeout,
      opts.isOplogReplay,
      opts.isPartial,
      opts.getMaxTime(TimeUnit.MILLISECONDS),
      TimeUnit.MILLISECONDS, opts.getCursorType)
  }

  implicit def findOptions2Mongo[T](opt: FindOptions[T])(implicit e: Encoder[T]) = {
    val mongoOpt = new MongoFindOptions
    mongoOpt.batchSize(opt.batchSize)
    mongoOpt.limit(opt.limit)
    mongoOpt.modifiers(BsonUtil.toBson(opt.modifiers))
    mongoOpt.projection(BsonUtil.toBson(opt.projection))
    mongoOpt.maxTime(opt.maxTime, opt.maxTimeUnit)
    mongoOpt.skip(opt.skip)
    mongoOpt.sort(BsonUtil.toBson(opt.sort))
    mongoOpt.noCursorTimeout(opt.noCursorTimeout)
    mongoOpt.oplogReplay(opt.oplogRelay)
    mongoOpt.partial(opt.partial)
    mongoOpt.cursorType(opt.cursorType)
    mongoOpt
  }
}
