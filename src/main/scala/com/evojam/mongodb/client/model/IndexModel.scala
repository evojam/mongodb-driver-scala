package com.evojam.mongodb.client.model

import java.util.concurrent.TimeUnit

import com.mongodb.bulk.IndexRequest
import com.mongodb.client.model.IndexOptions
import org.bson.codecs.Codec

import com.evojam.mongodb.client.util.BsonUtil

case class IndexModel[T](keys: T, options: IndexOptions) {
  require(keys != null, "keys cannot be null")
  require(options != null, "options cannot be null")

  def asIndexRequest()(implicit e: Codec[T]) =
    new IndexRequest(BsonUtil.toBson(keys))
      .name(options.getName)
      .background(options.isBackground)
      .unique(options.isUnique)
      .sparse(options.isSparse)
      .expireAfter(options.getExpireAfter(TimeUnit.SECONDS), TimeUnit.SECONDS)
      .version(options.getVersion)
      .weights(BsonUtil.toBsonDocument(options.getWeights))
      .defaultLanguage(options.getDefaultLanguage)
      .languageOverride(options.getLanguageOverride)
      .textVersion(options.getTextVersion)
      .sphereVersion(options.getSphereVersion)
      .bits(options.getBits)
      .min(options.getMin)
      .max(options.getMax)
      .bucketSize(options.getBucketSize)
      .storageEngine(BsonUtil.toBsonDocument(options.getStorageEngine))
}
