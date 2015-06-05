package com.evojam.mongodb.client.codec

import org.bson.codecs.Codec

trait Writer[T] {
  type R
  val codec: Codec[R]
  def write(doc: T): R
}
