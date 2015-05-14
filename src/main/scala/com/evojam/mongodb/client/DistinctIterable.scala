package com.evojam.mongodb.client

import scala.concurrent.duration.TimeUnit

import org.bson.conversions.Bson

trait DistinctIterable[T] extends MongoIterable[T] {

  def filter(filter: Bson): DistinctIterable[T]

  def maxTime(maxTime: Long, timeUnit: TimeUnit): DistinctIterable[T]

  def batchSize(size: Int): DistinctIterable[T]
}
