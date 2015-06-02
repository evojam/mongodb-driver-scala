package com.evojam.mongodb.client.iterable

import scala.concurrent.Future

import org.bson.codecs.Codec
import rx.lang.scala.Observable

trait MongoIterable {
  def head[T: Codec]: Future[T]

  def headOpt[T: Codec]: Future[Option[T]]

  def foreach[T: Codec](f: T => Unit): Unit

  def cursor[T: Codec](batchSize: Option[Int] = None): Observable[T]

  def collect[T: Codec](): Future[List[T]]
}
