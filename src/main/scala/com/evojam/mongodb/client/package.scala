package com.evojam.mongodb

import com.evojam.mongodb.client.codec.{Writer, Reader}
import org.bson.codecs.{Codec, DocumentCodec, BsonDocumentCodec}

package object client {
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
