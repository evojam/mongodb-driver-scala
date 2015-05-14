package com.evojam.mongodb.client

import scala.concurrent.duration.TimeUnit

trait ListIndexesIterable[T] extends MongoIterable[T] {

  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListIndexesIterable[T]

  def batchSize(size: Int): ListIndexesIterable[T]
}
