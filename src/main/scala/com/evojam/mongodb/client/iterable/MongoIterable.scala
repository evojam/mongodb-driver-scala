package com.evojam.mongodb.client.iterable

import scala.concurrent.Future

import rx.lang.scala.Observable

trait MongoIterable[T] {
  def head: Future[T]

  def headOpt: Future[Option[T]]

  def foreach(f: T => Unit): Unit

  def map[U](f: T => U): MongoIterable[U]

  def cursor(batchSize: Option[Int] = None): Observable[T]
}
