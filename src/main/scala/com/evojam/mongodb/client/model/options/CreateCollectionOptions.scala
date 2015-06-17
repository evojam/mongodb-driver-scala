package com.evojam.mongodb.client.model.options

import scala.language.implicitConversions

import com.mongodb.client.model.{ CreateCollectionOptions => MongoCreateCollectionOptions }
import org.bson.codecs.Encoder

import com.evojam.mongodb.client.util.BsonUtil

case class CreateCollectionOptions(
  capped: Boolean = CreateCollectionOptions.mongoOpts.isCapped(),
  maxDocuments: Long = CreateCollectionOptions.mongoOpts.getMaxDocuments(),
  autoIndex: Boolean = CreateCollectionOptions.mongoOpts.isAutoIndex(),
  size: Long = CreateCollectionOptions.mongoOpts.getSizeInBytes(),
  usePowerOf2Sizes: Boolean = false)

object CreateCollectionOptions {
  private lazy val mongoOpts = new MongoCreateCollectionOptions()
}
