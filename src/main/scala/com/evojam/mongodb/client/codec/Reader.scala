package com.evojam.mongodb.client.codec

import org.bson.codecs.Codec

trait Reader[T] {
  type R
  val codec: Codec[R]
  def read(doc: R): T
}
