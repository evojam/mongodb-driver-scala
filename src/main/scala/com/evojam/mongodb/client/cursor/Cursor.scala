package com.evojam.mongodb.client.cursor

import scala.concurrent.{ExecutionContext, Future}

import org.bson.codecs.Codec
import rx.lang.scala.Observable

import com.evojam.mongodb.client.codec.Reader

trait Cursor {
  def head[T]()(implicit r: Reader[T], exc: ExecutionContext): Future[T] =
    rawHead()(r.codec, exc)
      .toList
      .map(_.head)
      .map(r.read)
      .toBlocking.toFuture

  def headOpt[T]()(implicit r: Reader[T], exc: ExecutionContext): Future[Option[T]] =
    rawHead()(r.codec, exc)
      .toList
      .map(_.headOption)
      .map(_.map(r.read))
      .toBlocking.toFuture

  def foreach[T](f: T => Unit)(implicit r: Reader[T], exc: ExecutionContext): Unit =
    rawForeach((r.read _) andThen f)(r.codec, exc)

  def observable[T]()(implicit r: Reader[T], exc: ExecutionContext): Observable[T] =
    rawObservable()(r.codec, exc)
      .map(r.read)

  def observable[T](batchSize: Int)(implicit r: Reader[T], exc: ExecutionContext): Observable[List[T]] =
    rawObservable(batchSize)(r.codec, exc)
      .map(_.map(r.read))

  def collect[T]()(implicit r: Reader[T], exc: ExecutionContext): Future[List[T]] =
    rawObservable()(r.codec, exc)
      .map(r.read)
      .toList.toBlocking.toFuture

  protected def rawHead[T: Codec]()(implicit exc: ExecutionContext): Observable[T]

  protected def rawForeach[T: Codec](f: T => Unit)(implicit exc: ExecutionContext): Unit

  protected def rawObservable[T: Codec]()(implicit exc: ExecutionContext): Observable[T]

  protected def rawObservable[T: Codec](batchSize: Int)(implicit exc: ExecutionContext): Observable[List[T]]
}
