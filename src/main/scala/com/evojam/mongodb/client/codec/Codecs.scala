package com.evojam.mongodb.client.codec

import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.Codec
import org.bson.codecs.DocumentCodec

object Codecs {
  implicit lazy val bsonDocumentCodec = new BsonDocumentCodec()
  implicit lazy val documentCodec = new DocumentCodec()

  implicit def identityReader[T: Codec]: Reader[T] = new Reader[T] {
    override type R = T
    override val codec = implicitly[Codec[T]]
    override def read(doc: T): T = doc
  }

  implicit def identityWriter[T: Codec]: Writer[T] = new Writer[T] {
    override type R = T
    override val codec = implicitly[Codec[T]]
    override def write(doc: T): T = doc
  }
}
