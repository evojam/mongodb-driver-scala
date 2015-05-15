package com.evojam.mongodb.client.iterable

// TODO: Think about execution context we want to use

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.evojam.mongodb.client.util.AsyncEnriched
import rx.lang.scala.Observable

case class ResultIterable[T](wrapped: Observable[T]) extends MongoIterable[T] with AsyncEnriched {

  override def head = headOpt.map(_.get)

  override def cursor(batchSize: Option[Int]) = wrapped

  override def headOpt: Future[Option[T]] =
    wrapped
      .first
      .toList
      .map(_.headOption).toBlocking.toFuture

  override def foreach(f: T => Unit) = wrapped.foreach(f)

  override def map[U](f: T => U) = ResultIterable(wrapped.map(f))
}
