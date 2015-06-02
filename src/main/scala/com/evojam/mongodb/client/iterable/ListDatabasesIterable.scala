package com.evojam.mongodb.client.iterable

import java.util.concurrent.TimeUnit.MILLISECONDS

import scala.concurrent.duration.TimeUnit

import com.mongodb.ReadPreference
import com.mongodb.operation.ListDatabasesOperation
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecRegistry

import com.evojam.mongodb.client.ObservableOperationExecutor

case class ListDatabasesIterable(
  codecRegistry: CodecRegistry,
  readPreference: ReadPreference,
  executor: ObservableOperationExecutor,
  maxTimeMS: Long = 0) extends MongoIterable {

  def maxTime(maxTime: Long, timeUnit: TimeUnit): ListDatabasesIterable =
    this.copy(maxTimeMS = MILLISECONDS.convert(maxTime, timeUnit))

  private def createOperation[T]()(implicit c: Codec[T]): ListDatabasesOperation[T] =
    new ListDatabasesOperation[T](c).maxTime(maxTimeMS, MILLISECONDS)

  def executedOperation[T: Codec] = OperationIterable(createOperation, readPreference, executor)

  override def cursor[T: Codec](batchSize: Option[Int]) = executedOperation.cursor(batchSize)

  override def headOpt[T: Codec] = executedOperation.headOpt

  override def foreach[T: Codec](f: T => Unit) = executedOperation.foreach(f)

  override def head[T: Codec] = executedOperation.head

  override def collect[T: Codec]() = executedOperation.collect()
}
