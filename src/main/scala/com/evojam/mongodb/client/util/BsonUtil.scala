package com.evojam.mongodb.client.util

import org.bson.BsonDocument
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

object BsonUtil {
  def toBsonDocument(bson: Bson)
    (implicit documentClass: Class[_], codecRegistry: CodecRegistry): BsonDocument =
    if (bson == null) {
      null
    } else {
      bson.toBsonDocument(documentClass, codecRegistry)
    }
}
