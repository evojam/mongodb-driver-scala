package com.evojam.mongodb.client.iterable

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.TimeUnit

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.DistinctOperation
import com.evojam.mongodb.client.model.DistinctOperation.distinctOperation2MongoOperation
import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.DistinctOperation
import com.evojam.mongodb.client.model.DistinctOperation.distinctOperation2MongoOperation

case class DistinctIterable[TDoc <: Any : Manifest, TRes <: Any : Manifest]( // scalastyle:ignore
  fieldName: String,
  filter: Bson,
  namespace: MongoNamespace,
  readPreference: ReadPreference,
  codecRegistry: CodecRegistry,
  executor: ObservableOperationExecutor,
  private val maxTimeMS: Option[Long] = None) extends MongoIterable[TRes] {

  require(fieldName != null, "fieldName cannot be null")
  require(filter != null, "filter cannot be null")
  require(namespace != null, "namespace cannot be null")
  require(readPreference != null, "readPreference cannot be null")
  require(codecRegistry != null, "codecRegistry cannot be null")
  require(executor != null, "executor cannot be null")

  private val documentClass = manifest[TDoc].runtimeClass.asInstanceOf[Class[TDoc]]
  private val resultClass = manifest[TRes].runtimeClass.asInstanceOf[Class[TRes]]

  def head = execute.head

  def headOpt = execute.headOpt

  def foreach(f: TRes => Unit) = execute.foreach(f)

  def map[U](f: TRes => U) = MappingIterable[TRes, U](this, f)

  def cursor(batchSize: Option[Int] = None) = execute.cursor(batchSize)

  def collect() = execute.collect

  def filter(filter: Bson) = this.copy[TDoc, TRes](filter = filter)

  def maxTime(maxTime: Long, timeUnit: TimeUnit) = {
    require(timeUnit != null, "timeUnit cannot be null")
    this.copy[TDoc, TRes](maxTimeMS = Some(TimeUnit.MILLISECONDS.convert(maxTime, timeUnit)))
  }

  private def execute(): MongoIterable[TRes] = execute(distinctOperation)

  private def execute(operation: DistinctOperation[TRes]): MongoIterable[TRes] =
    new OperationIterable[TRes](operation, readPreference, executor)

  private def distinctOperation =
    DistinctOperation[TRes](
      namespace,
      fieldName,
      codecRegistry.get(resultClass),
      Some(filter.toBsonDocument(documentClass, codecRegistry)),
      maxTimeMS)
}
