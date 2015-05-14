package com.evojam.mongodb.client.iterable

import scala.concurrent.Future

import rx.Observable

trait MongoIterable[T] {
  def head: Future[T]
  def headOpt: Future[Option[T]]
  def foreach(f: T => Unit): Future[Unit]
  def map[U](f: T => U): MongoIterable[U]
  def collect(batchSize: Option[Int] = None): Observable[T]
}
