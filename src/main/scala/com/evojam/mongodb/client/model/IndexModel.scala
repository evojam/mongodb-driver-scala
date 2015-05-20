package com.evojam.mongodb.client.model

import java.util.concurrent.TimeUnit

import com.mongodb.bulk.IndexRequest
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

import com.evojam.mongodb.client.model.options.IndexOptions
import com.evojam.mongodb.client.util.BsonUtil

case class IndexModel(keys: Bson, options: IndexOptions) {
  require(keys != null, "keys cannot be null")
  require(options != null, "options cannot be null")

  def asIndexRequest()(implicit documentClass: Class[_], codecRegistry: CodecRegistry) =
    new IndexRequest(BsonUtil.toBsonDocument(keys))
      .name(options.name)
      .background(options.background)
      .unique(options.unique)
      .sparse(options.sparse)
      .expireAfter(TimeUnit.SECONDS.convert(options.expireAfterSeconds, TimeUnit.SECONDS), TimeUnit.SECONDS)
      .version(options.version)
      .weights(BsonUtil.toBsonDocument(options.weights))
      .defaultLanguage(options.defaultLanguage)
      .languageOverride(options.languageOverride)
      .textVersion(options.textVersion)
      .sphereVersion(options.sphereVersion)
      .bits(options.bits)
      .min(options.min)
      .max(options.max)
      .bucketSize(options.bucketSize)
      .storageEngine(BsonUtil.toBsonDocument(options.storageEngine))
}