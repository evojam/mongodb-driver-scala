package com.evojam.mongodb.client.model

import java.util.concurrent.TimeUnit

import scala.language.implicitConversions

import com.mongodb.client.model.{ FindOptions => MongoFindOptions }
import com.mongodb.CursorType
import org.bson.conversions.Bson

case class FindOptions(
  batchSize: Int,
  limit: Int,
  modifiers: Bson,
  projection: Bson,
  skip: Int,
  sort: Bson,
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
  def apply(): FindOptions = {
    val opts = new MongoFindOptions()
    FindOptions(opts.getBatchSize, opts.getLimit, opts.getModifiers,
        opts.getProjection, opts.getSkip, opts.getSort, opts.isNoCursorTimeout,
        opts.isOplogReplay, opts.isPartial, opts.getMaxTime(TimeUnit.MILLISECONDS),
        TimeUnit.MILLISECONDS, opts.getCursorType)
  }

  implicit def findOptions2Mongo(opt: FindOptions) = {
    val mongoOpt = new MongoFindOptions
    mongoOpt.batchSize(opt.batchSize)
    mongoOpt.limit(opt.limit)
    mongoOpt.modifiers(opt.modifiers)
    mongoOpt.projection(opt.projection)
    mongoOpt.maxTime(opt.maxTime, opt.maxTimeUnit)
    mongoOpt.skip(opt.skip)
    mongoOpt.sort(opt.sort)
    mongoOpt.noCursorTimeout(opt.noCursorTimeout)
    mongoOpt.oplogReplay(opt.oplogRelay)
    mongoOpt.partial(opt.partial)
    mongoOpt.cursorType(opt.cursorType)
    mongoOpt
  }
}
