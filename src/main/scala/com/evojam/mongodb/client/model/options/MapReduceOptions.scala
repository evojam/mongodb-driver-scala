package com.evojam.mongodb.client.model.options

import org.bson.codecs.Encoder

import com.evojam.mongodb.client.model.options.MapReduceAction.MapReduceAction

case class MapReduceOptions[T: Encoder](
  action: MapReduceAction = MapReduceAction.Replace,
  verbose: Boolean = true,
  scope: Option[T] = None,
  filter: Option[T] = None,
  sort: Option[T] = None,
  limit: Option[Int] = None,
  finalizeFunction: Option[String] = None,
  jsMode: Option[Boolean] = None,
  maxTimeMS: Option[Long] = None,
  sharded: Option[Boolean] = None,
  nonAtomic: Option[Boolean] = None,
  batchSize: Option[Int] = None) {

  require(action != null, "action cannot be null")
  require(scope != null, "scope cannot be null")
  require(filter != null, "filter cannot be null")
  require(sort != null, "sort cannot be null")
  require(limit != null, "limit cannot be null")
  require(finalizeFunction != null, "finalizeFunction cannot be null")
  require(jsMode != null, "jsMode cannot be null")
  require(maxTimeMS != null, "maxTimeMS cannot be null")
  require(sharded != null, "sharded cannot be null")
  require(nonAtomic != null, "nonAtomic cannot be null")
  require(batchSize != null, "batchSize cannot be null")
}
