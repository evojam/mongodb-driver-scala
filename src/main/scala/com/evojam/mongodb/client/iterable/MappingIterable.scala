package com.evojam.mongodb.client.iterable

import scala.concurrent.ExecutionContext.Implicits.global

case class MappingIterable[T, U](
  iterable: MongoIterable[T],
  mapper: T => U) extends MongoIterable[U] {

  require(iterable != null, "iterable cannot be null")
  require(mapper != null, "mapper cannot be null")

  lazy val mapped = iterable.map(mapper)

  override def head =
    iterable.head.map(mapper)

  override def headOpt =
    iterable.headOpt.map(_.map(mapper))

  override def foreach(f: U => Unit) =
    iterable.foreach(mapper andThen f)

  override def map[V](f: U => V) =
    MappingIterable(this, f)

  override def cursor(batchSize: Option[Int]) =
    iterable.cursor(batchSize).map(mapper)

  override def collect() = iterable.map(mapper).collect()
}
