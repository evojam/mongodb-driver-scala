package com.evojam.mongodb.client.model.bulk

import com.mongodb.bulk.{DeleteRequest, UpdateRequest, InsertRequest, WriteRequest}
import org.bson.codecs.Codec

import com.evojam.mongodb.client.util.BsonUtil.toBson

sealed trait WriteModel

case class Insert[R: Codec](document: R) extends WriteModel {
  require(document != null, "document cannot be null")

  def documentBson() = toBson(document)
}

case class Replace[T: Codec, R: Codec](
  filter: T,
  replace: R,
  upsert: Boolean = false) extends WriteModel {

  require(filter != null, "filter cannot be null")
  require(replace != null, "replace cannot be null")

  def filterBson() = toBson(filter)
  def replaceBson() = toBson(replace)
}

case class Update[T: Codec](
  filter: T,
  update: T,
  upsert: Boolean = false,
  many: Boolean = false) extends WriteModel {

  require(filter !=  null, "filter cannot be null")
  require(update != null, "update cannot be null")

  def filterBson() = toBson(filter)
  def updateBson() = toBson(update)
}

case class Delete[T: Codec](filter: T, many: Boolean = false) extends WriteModel {
  require(filter != null, "filter cannot be null")

  def filterBson() = toBson(filter)
}
