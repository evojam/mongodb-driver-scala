package com.evojam.mongodb.client.iterable

import java.util.concurrent.TimeUnit

import scala.concurrent.Future

import com.mongodb.CursorType
import org.bson.conversions.Bson
import rx.Observable

class FindIterable[T] extends MongoIterable[T] {
  def filter(filter: Bson): FindIterable[T] = ???
  def limit(limit: Int): FindIterable[T] = ???
  def skip(limit: Int): FindIterable[T] = ???
  def maxTime(maxTime: Long, timeUnit: TimeUnit): FindIterable[T] = ???
  def modifiers(modifiers: Bson): FindIterable[T] = ???
  def projection(projection: Bson): FindIterable[T] = ???
  def sort(sort: Bson): FindIterable[T] = ???
  def noCursorTimeout(noCursorTimeout: Boolean): FindIterable[T] = ???
  def oplogRelay(oplogRelay: Boolean): FindIterable[T] = ???
  def partial(partial: Boolean): FindIterable[T] = ???
  def cursorType(cursorType: CursorType): FindIterable[T] = ???
  def batchSize(batchSize: Int): FindIterable[T] = ???

  def head: Future[T] = ???
  def headOpt: Future[Option[T]] = ???
  def foreach(f: T => Unit): Future[Unit] = ???
  def map[U](f: T => U): MongoIterable[U] = ???
  def collect(batchSize: Option[Int]): Observable[T] = ???
}