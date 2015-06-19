package com.evojam.mongodb.client.util

import java.nio.ByteBuffer

import scala.collection.JavaConversions._

import org.bson._
import org.bson.codecs._
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

  def fromBsonValue[T](bsonValue: BsonValue)(implicit c: Codec[T]): Option[T] =
    Option(bsonValue)
      .map(value =>
        c.decode(
          new BsonBinaryReader(ByteBuffer.wrap(value.asBinary().getData())),
          DecoderContext.builder().build()))

  def toBsonDocument[T](bson: Bson)(implicit c: Codec[T]): BsonDocument =
    if (bson == null) {
      null
    } else {
      bson.toBsonDocument(
        c.getEncoderClass,
        CodecRegistries.fromCodecs(List(c)))
    }

  def copy(doc: Document): Document = {
    val documentCopy = new Document()
    for(key <- doc.keySet())
      documentCopy.append(key, doc.get(key))
    documentCopy
  }
}
