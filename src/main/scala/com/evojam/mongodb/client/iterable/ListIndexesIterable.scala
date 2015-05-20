package com.evojam.mongodb.client.iterable

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.ListIndexesOperation

import com.mongodb.{ MongoNamespace, ReadPreference }

import org.bson.codecs.configuration.CodecRegistry

case class ListIndexesIterable[TDoc <: Any : Manifest, TRes <: Any : Manifest](//scalastyle:ignore
  namespace: MongoNamespace,
  readPreference: ReadPreference,
  codecRegistry: CodecRegistry,
  maxTime: Long = 0L,
  batchSize: Int = 0,
  executor: ObservableOperationExecutor) extends MongoIterable[TRes] {

  private val documentClass = manifest[TDoc].runtimeClass

  def maxTime(time: Long): ListIndexesIterable[TDoc, TRes] =
    this.copy(maxTime = time)

  def batchSize(size: Int): ListIndexesIterable[TDoc, TRes] =
    this.copy(batchSize = size)

  private def queryOperation: ListIndexesOperation[TRes] =
    ListIndexesOperation[TRes](
      namespace,
      codecRegistry.get(documentClass.asInstanceOf[Class[TRes]]),
      batchSize,
      maxTime)

  private def execute: MongoIterable[TRes] =
    execute(queryOperation)

  private def execute(lio: ListIndexesOperation[TRes]) =
    new OperationIterable[TRes](lio, readPreference, executor)

  override def head =
    execute(queryOperation.copy(batchSize = -1)).head

  override def collect() =
    execute.collect()

  override def cursor(batchSize: Option[Int]) =
    execute.cursor(batchSize)

  override def headOpt =
    execute(queryOperation.copy(batchSize = -1)).headOpt

  override def foreach(f: (TRes) => Unit) =
    execute.foreach(f)

  override def map[U](f: TRes => U) =
    MappingIterable[TRes, U](this, f)
}
