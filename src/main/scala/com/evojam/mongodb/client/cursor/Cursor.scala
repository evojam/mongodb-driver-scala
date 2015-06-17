package com.evojam.mongodb.client.cursor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.bson.codecs.Codec
import rx.lang.scala.Observable

import com.evojam.mongodb.client.codec.Reader

trait Cursor {
  def head[T]()(implicit r: Reader[T]): Future[T] =
    rawHead()(r.codec).map(r.read)

  def headOpt[T]()(implicit r: Reader[T]): Future[Option[T]] =
    rawHeadOpt()(r.codec).map(_.map(r.read))

  def foreach[T](f: T => Unit)(implicit r: Reader[T]): Unit =
    rawForeach((r.read _) andThen f)(r.codec)

  def observable[T]()(implicit r: Reader[T]): Observable[T] =
    rawObservable()(r.codec).map(r.read)

  def observable[T](batchSize: Int)(implicit r: Reader[T]): Observable[List[T]] =
    rawObservable(batchSize)(r.codec).map(_.map(r.read))

  def collect[T]()(implicit r: Reader[T]): Future[List[T]] =
    rawCollect()(r.codec).map(_.map(r.read))

  protected def rawHead[T: Codec](): Future[T]

  protected def rawHeadOpt[T: Codec](): Future[Option[T]]

  protected def rawForeach[T: Codec](f: T => Unit): Unit

  protected def rawObservable[T: Codec](): Observable[T]

  protected def rawObservable[T: Codec](batchSize: Int): Observable[List[T]]

  protected def rawCollect[T: Codec](): Future[List[T]]
}
