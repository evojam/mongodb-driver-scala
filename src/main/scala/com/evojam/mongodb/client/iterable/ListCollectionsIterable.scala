package com.evojam.mongodb.client.iterable

import scala.concurrent.Future

import com.mongodb.ReadPreference
import org.bson.BsonDocument
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.operation.ListCollectionOperation

case class ListCollectionsIterable[TDoc <: Any : Manifest]( // scalastyle:ignore
  dbName: String,
  codecRegistry: CodecRegistry,
  readPreference: ReadPreference,
  executor: ObservableOperationExecutor,
  filter: Option[Bson] = None,
  maxTime: Option[Long] = None,
  batchSize: Option[Int] = None) extends MongoIterable[TDoc] {

  private val documentClass = manifest[TDoc].runtimeClass

  def filter(filter: Bson): ListCollectionsIterable[TDoc] = {
    require(filter != null, "filter cannot be null")
    this.copy(filter = Some(filter))
  }

  def maxTime(time: Long): ListCollectionsIterable[TDoc] = {
    require(time >= 0L, "time cannot be negative")
    this.copy(maxTime = Some(time))
  }

  def batchSize(size: Int): ListCollectionsIterable[TDoc] = {
    require(size >= 0, "size cannot be negative")
    this.copy(batchSize = Some(size))
  }

  private def execute: MongoIterable[TDoc] =
    execute(queryOperation)

  private def execute(lco: ListCollectionOperation[TDoc]): MongoIterable[TDoc] =
    new OperationIterable[TDoc](lco, readPreference, executor)

  private def queryOperation: ListCollectionOperation[TDoc] =
    ListCollectionOperation[TDoc](
      dbName,
      codecRegistry.get(documentClass.asInstanceOf[Class[TDoc]]),
      filter.map(toBsonDocument),
      batchSize,
      maxTime)

  override def head: Future[TDoc] =
    execute(queryOperation.copy(batchSize = Some(-1))).head

  override def map[TRes](f: TDoc => TRes): MappingIterable[TDoc, TRes] =
    MappingIterable[TDoc, TRes](this, f)

  override def headOpt =
    execute(queryOperation.copy(batchSize = Some(-1))).headOpt

  override def cursor(batchSize: Option[Int]) =
    execute.cursor(batchSize)

  override def foreach(f: TDoc => Unit) =
    execute.foreach(f)

  private def toBsonDocument(bson: Bson): BsonDocument =
    if (bson == null) {
      null
    } else {
      bson.toBsonDocument(documentClass, codecRegistry)
    }

  override def collect() = execute.collect()
}
