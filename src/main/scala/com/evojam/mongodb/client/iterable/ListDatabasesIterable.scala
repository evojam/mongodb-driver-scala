package com.evojam.mongodb.client.iterable

import java.util.concurrent.TimeUnit._

import scala.concurrent.duration.TimeUnit

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.mongodb.ReadPreference
import com.mongodb.operation.ListDatabasesOperation
import org.bson.codecs.configuration.CodecRegistry

trait ListDatabasesIterable[T] extends MongoIterable[T] {
  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListDatabasesIterable[T]
}

case class ListDatabasesIterableImpl[T](resultClass: Class[T], codecRegistry: CodecRegistry,
  readPreference: ReadPreference, executor: ObservableOperationExecutor,
  maxTimeMS: Long = 0) extends ListDatabasesIterable[T] {

  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListDatabasesIterable[T] =
    this.copy[T](maxTimeMS = MILLISECONDS.convert(maxTime, timeUnit))

  private def createOperation: ListDatabasesOperation[T] =
    new ListDatabasesOperation[T](codecRegistry.get(resultClass)).maxTime(maxTimeMS, MILLISECONDS)

  lazy val executedOperation = OperationIterable(createOperation, readPreference, executor)

  override def cursor(batchSize: Option[Int]) = executedOperation.cursor(batchSize)

  override def headOpt = executedOperation.headOpt

  override def foreach(f: (T) => Unit) = executedOperation.foreach(f)

  override def map[U](f: (T) => U) = executedOperation.map(f)

  override def head = executedOperation.head

  override def collect() = executedOperation.collect()
}