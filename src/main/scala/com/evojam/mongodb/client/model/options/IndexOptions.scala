package com.evojam.mongodb.client.model.options

import java.util.concurrent.TimeUnit

import org.bson.conversions.Bson

import com.mongodb.client.model.{ IndexOptions => MongoIndexOptions }

case class IndexOptions(
  validTextIndexVersions: List[Int] = List(1, 2),
  validSpehreIndexVersions: List[Int] = List(1, 2),
  background: Boolean,
  unique: Boolean,
  name: String,
  sparse: Boolean,
  expireAfterSeconds: Long,
  version: Int,
  weights: Bson,
  defaultLanguage: String,
  languageOverride: String,
  textVersion: Int,
  sphereVersion: Int,
  bits: Int,
  min: Double,
  max: Double,
  bucketSize: Double,
  storageEngine: Bson) {

  require(name != null, "name cannot be null")
  require(expireAfterSeconds > 0, "expireAfterSeconds cannot be negative or zero")
  require(version >= 0, "version cannot be negative")
  require(weights != null, "weights cannot be null")
  require(defaultLanguage != null, "defaultLanguage cannot be null")
  require(languageOverride != null, "lanaguageOverride cannot be null")
  require(textVersion >= 0, "textVersion cannot be negative")
  require(sphereVersion >= 0, "sphereVersion cannot be negative")
  require(bits >= 0, "bits cannot be negative")
  require(min >= 0, "min cannot be negative")
  require(max >= 0, "max cannot be negative")
  require(bucketSize >= 0, "bucketSize cannot be negative")
  require(storageEngine != null, "storageEngine cannot be null")
}

object IndexOptions {
  def apply(): IndexOptions = {
    val opts = new MongoIndexOptions()
    IndexOptions(
      background = opts.isBackground,
      unique = opts.isUnique,
      name = opts.getName,
      sparse = opts.isSparse,
      expireAfterSeconds = opts.getExpireAfter(TimeUnit.SECONDS),
      version = opts.getVersion,
      weights = opts.getWeights,
      defaultLanguage = opts.getDefaultLanguage,
      languageOverride = opts.getLanguageOverride,
      textVersion = opts.getTextVersion,
      sphereVersion = opts.getSphereVersion,
      bits = opts.getBits,
      min = opts.getMin,
      max = opts.getMax,
      bucketSize = opts.getBucketSize,
      storageEngine = opts.getStorageEngine)
  }
}