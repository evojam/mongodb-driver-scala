package com.evojam.mongodb.client.util

import scala.collection.JavaConversions.seqAsJavaList

import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.Encoder
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistries
import org.bson.conversions.Bson

object BsonUtil {
  def toBson[T](doc: T)(implicit e: Encoder[T]): BsonDocument = {
    val res = new BsonDocument()
    e.encode(
      new BsonDocumentWriter(res),
      doc,
      EncoderContext.builder().build())
    res
  }

  def toBson[T: Encoder](maybeDoc: Option[T]): BsonDocument =
    maybeDoc.map(toBson(_)).getOrElse(null)

  def fromBson[T](doc: Bson)(implicit c: Codec[T]): Option[T] = {
    val bsonDoc = toBsonDocument(doc)
    if (bsonDoc != null) {
      Some(c.decode(new BsonDocumentReader(bsonDoc), DecoderContext.builder().build()))
    } else {
      None
    }
  }

  def toBsonDocument[T](bson: Bson)(implicit c: Codec[T]): BsonDocument =
    if (bson == null) {
      null
    } else {
      bson.toBsonDocument(
        c.getEncoderClass,
        CodecRegistries.fromCodecs(List(c)))
    }
}
