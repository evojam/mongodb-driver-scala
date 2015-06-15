package com.evojam.mongodb.client.iterable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.bson.codecs.Codec
import rx.lang.scala.Observable

import com.evojam.mongodb.client.codec.Reader

trait MongoIterable {
  def head[T]()(implicit r: Reader[T]): Future[T] =
    rawHead()(r.codec).map(r.read)

  def headOpt[T]()(implicit r: Reader[T]): Future[Option[T]] =
    rawHeadOpt()(r.codec).map(_.map(r.read))

  def foreach[T](f: T => Unit)(implicit r: Reader[T]): Unit =
    rawForeach((r.read _) andThen f)(r.codec)

  def cursor[T]()(implicit r: Reader[T]): Observable[T] =
    rawCursor()(r.codec).map(r.read)

  def cursor[T](batchSize: Int)(implicit r: Reader[T]): Observable[List[T]] =
    rawCursor(batchSize)(r.codec).map(_.map(r.read))

  def collect[T]()(implicit r: Reader[T]): Future[List[T]] =
    rawCollect()(r.codec).map(_.map(r.read))

  protected def rawHead[T: Codec](): Future[T]

  protected def rawHeadOpt[T: Codec](): Future[Option[T]]

  protected def rawForeach[T: Codec](f: T => Unit): Unit

  protected def rawCursor[T: Codec](): Observable[T]

  protected def rawCursor[T: Codec](batchSize: Int): Observable[List[T]]

  protected def rawCollect[T: Codec](): Future[List[T]]
}
