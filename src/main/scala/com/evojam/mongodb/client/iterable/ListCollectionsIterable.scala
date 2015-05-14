package com.evojam.mongodb.client.iterable

import scala.concurrent.Future

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.mongodb.ReadPreference
import com.mongodb.async.AsyncBatchCursor
import com.mongodb.operation.ListCollectionsOperation
import org.bson.BsonDocument
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

class ListCollectionsIterable[T](dbName: String, resultClass: Class[T], codecRegistry: CodecRegistry,
  readPreference: ReadPreference, executor: ObservableOperationExecutor) extends MongoIterable[T] {

  def filter(filter: Bson): ListCollectionsIterable[T] = ???

  def maxTime(time: Long): ListCollectionsIterable[T] = ???

  def batchSize(size: Int): ListCollectionsIterable[T] = ???

  def batchCursor(): Future[AsyncBatchCursor[T]] = ???

  private def execute(): MongoIterable[T] = ???

  private def execute(operation: ListCollectionsOperation[T]): MongoIterable[T] = ???

  private def createListCollectionsOperation(): ListCollectionsOperation[T] = ???

  private def toBsonDocument(document: Bson): BsonDocument = ???

  override def head(): Future[T] = ???

  override def map[U](f: T => U): MappingIterable[T, U] = ???

  override def headOpt = ???

  override def cursor(batchSize: Option[Int]) = ???

  override def foreach(f: (T) => Unit) = ???
}
