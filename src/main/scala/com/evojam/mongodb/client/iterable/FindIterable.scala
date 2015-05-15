package com.evojam.mongodb.client.iterable

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.{ FindOperation, FindOptions }
import com.mongodb.{ MongoNamespace, ReadPreference }
import org.bson.BsonDocument
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson


case class FindIterable[TDoc <: Any : Manifest, TRes <: Any : Manifest](// scalastyle:ignore
  filter: Bson,
  findOptions: FindOptions,
  namespace: MongoNamespace,
  readPreference: ReadPreference,
  codecRegistry: CodecRegistry,
  executor: ObservableOperationExecutor) extends MongoIterable[TRes] {

  require(filter != null, "filter cannot be null")
  require(findOptions != null, "findOptions cannot be null")
  require(namespace != null, "namespace cannot be null")
  require(readPreference != null, "readPreference cannot be null")
  require(codecRegistry != null, "codecRegistry cannot be null")
  require(executor != null, "executor cannt be null")

  private val documentClass = manifest[TDoc].runtimeClass
  private val resultClass = manifest[TRes]

  override def head =
    execute(queryOperation.copy(batchSize = 0, limit = -1)).head

  override def headOpt =
    execute(queryOperation.copy(batchSize = 0, limit = -1)).headOpt

  override def foreach(f: TRes => Unit) =
    execute.foreach(f)

  override def map[U](f: TRes => U) =
    MappingIterable[TRes, U](this, f)

  override def cursor(batchSize: Option[Int]) =
    execute.cursor(batchSize)

  private def execute: MongoIterable[TRes] = execute(queryOperation)

  private def execute(fo: FindOperation[TRes]): MongoIterable[TRes] =
    new OperationIterable[TRes](fo, readPreference, executor)

  private def queryOperation =
    FindOperation[TRes](
      namespace = namespace,
      decoder = codecRegistry.get(resultClass.asInstanceOf[Class[TRes]]),
      filter = bsonDocument(filter),
      batchSize = findOptions.batchSize,
      skip = findOptions.skip,
      limit = findOptions.limit,
      maxTime = findOptions.maxTime,
      maxTimeUnit = findOptions.maxTimeUnit,
      modifiers = bsonDocument(findOptions.modifiers),
      projection = bsonDocument(findOptions.projection),
      sort = bsonDocument(findOptions.sort),
      cursorType = findOptions.cursorType,
      noCursorTimeout = findOptions.noCursorTimeout,
      oplogRelay = findOptions.oplogRelay,
      partial = findOptions.partial,
      slaveOk = readPreference.isSlaveOk)

  private def bsonDocument(bson: Bson): BsonDocument =
    if(bson == null) {
      null
    } else {
      bson.toBsonDocument(documentClass, codecRegistry)
    }

  override def collect() = execute.collect()
}