package com.evojam.mongodb.client.iterable

// TODO: Think about execution context we want to use
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.bson.codecs.Codec
import rx.lang.scala.Observable

import com.evojam.mongodb.client.util.AsyncEnriched

private[client] case class ResultIterable[R: Codec](wrapped: Observable[R]) extends AsyncEnriched {

  def head: Future[R] = headOpt.map(_.get)

  def headOpt: Future[Option[R]] =
    wrapped
      .first
      .toList
      .map(_.headOption).toBlocking.toFuture

  def foreach(f: R => Unit): Unit = wrapped.foreach(f)

  def cursor(batchSize: Option[Int]): Observable[R] = wrapped

  def collect(): Future[List[R]] = wrapped.toList.toBlocking.toFuture
}
