package com.evojam.mongodb.client.iterable

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.util.AsyncEnriched
import com.mongodb.ReadPreference
import com.mongodb.async.AsyncBatchCursor
import com.mongodb.operation.AsyncReadOperation

class OperationIterable[T](operation: AsyncReadOperation[_ <: AsyncBatchCursor[T]], readPreference: ReadPreference,
  executor: ObservableOperationExecutor) extends MongoIterable[T] with AsyncEnriched {

  def batchSize(batchSize: Int): OperationIterable[T] = throw new UnsupportedOperationException

  def observable() = executor.execute(operation, readPreference).flatMap(_.asObservable)

  override def head = ???

  override def collect(batchSize: Option[Int]) = ???

  override def headOpt = ???

  override def foreach(f: (T) => Unit) = ???

  override def map[U](f: (T) => U) = ???
}
