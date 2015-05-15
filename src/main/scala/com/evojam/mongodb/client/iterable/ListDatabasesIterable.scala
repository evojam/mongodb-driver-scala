package com.evojam.mongodb.client.iterable

import java.util.concurrent.TimeUnit._

import scala.concurrent.duration.TimeUnit

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.mongodb.ReadPreference
import com.mongodb.operation.ListDatabasesOperation
import org.bson.codecs.configuration.CodecRegistry

trait ListDatabasesIterable[T] extends MongoIterable[T] {
  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListDatabasesIterable[T]

  def batchSize(batchSize: Int): ListDatabasesIterable[T]
}

case class ListDatabasesIterableImpl[T](resultClass: Class[T], codecRegistry: CodecRegistry,
  readPreference: ReadPreference, executor: ObservableOperationExecutor,
  maxTimeMS: Long = 0) extends ListDatabasesIterable[T] {

  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListDatabasesIterable[T] =
    this.copy[T](maxTimeMS = MILLISECONDS.convert(maxTime, timeUnit))

  //  private def execute: MongoIterable[T] = {
  //    return execute(createListDatabasesOperation)
  //  }
  //
  //  private def execute(operation: ListDatabasesOperation[T]): MongoIterable[T] = {
  //    return new OperationIterable[T](operation, readPreference, executor)
  //  }
  //
  // class ListDatabasesOperation<T> implements AsyncReadOperation<AsyncBatchCursor<T>>, ReadOperation<BatchCursor<T>>

  private def createOperation: ListDatabasesOperation[T] =
    new ListDatabasesOperation[T](codecRegistry.get(resultClass)).maxTime(maxTimeMS, MILLISECONDS)

  private def execute() = {
    val x = executor.execute(createOperation, readPreference)
    ???
  }

  override def batchSize(batchSize: Int) = ???

  override def cursor(batchSize: Option[Int]) = ???

  override def headOpt = ???

  override def foreach(f: (T) => Unit) = ???

  override def map[U](f: (T) => U) = ???

  override def head = ???
}