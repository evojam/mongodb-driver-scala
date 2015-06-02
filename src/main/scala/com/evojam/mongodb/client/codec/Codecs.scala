package com.evojam.mongodb.client.codec

import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.DocumentCodec

object Codecs {
  implicit lazy val bsonDocumentCodec = new BsonDocumentCodec()
  implicit lazy val documentCodec = new DocumentCodec()
}
