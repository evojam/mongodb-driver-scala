package com.evojam.mongodb.client

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

import com.mongodb._
import com.mongodb.bulk._
import com.mongodb.client.model._
import com.mongodb.client.result.DeleteResult
import com.mongodb.operation._
import org.bson.codecs.Codec

import com.evojam.mongodb.client.builder.FindAndModifyBuilder
import com.evojam.mongodb.client.cursor._
import com.evojam.mongodb.client.model.operation.{CountOperation, CreateIndexesOperation, DropIndexOperation}
import com.evojam.mongodb.client.model.options.{FindOptions, MapReduceOptions}
import com.evojam.mongodb.client.model.result.UpdateResult
import com.evojam.mongodb.client.model.{IndexModel, WriteOperation}
import com.evojam.mongodb.client.util.BsonUtil
import com.evojam.mongodb.client.util.Conversions._

private[client] case class MongoCollectionImpl(
  namespace: MongoNamespace,
  implicit val readPreference: ReadPreference,
  implicit val writeConcern: WriteConcern,
  private val executor: ObservableOperationExecutor) extends MongoCollection {

  require(namespace != null, "namespace cannot be null")
  require(readPreference != null, "readPreference cannot be null")
  require(writeConcern != null, "writeConcern cannot be null")
  require(executor != null, "executor cannot be null")

  override def withReadPreference(readPreference: ReadPreference) =
    this.copy(readPreference = readPreference)

  override def withWriteConcern(writeConcern: WriteConcern) =
    this.copy(writeConcern = writeConcern)

  override def count[T: Codec](filter: T, options: CountOptions) =
    executor.executeAsync(
      CountOperation(namespace, Option(filter), options),
      readPreference).toBlocking.toFuture.map(_.longValue)

  override def find[T: Codec](filter: T) =
    FindCursor(Option(filter), FindOptions(), namespace, readPreference, executor)

  override def distinct[T: Codec](fieldName: String, filter: T) =
    DistinctCursor(fieldName, Option(filter), namespace, readPreference, executor)

  override def aggregate[T: Codec](pipeline: List[T]) =
    AggregateCursor(pipeline, namespace, readPreference, executor)

  def mapReduce[T: Codec](mapFunction: String, reduceFunction: String): MapReduceCursor[T] =
    MapReduceCursor(mapFunction, reduceFunction, readPreference, namespace, executor, MapReduceOptions[T]())

  // TODO: Bulk write/read

  override protected def rawInsert[T: Codec](document: T) =
    executeWrite(new InsertRequest(BsonUtil.toBson(document)))(_ => ())

  override protected def rawInsertAll[T: Codec](documents: List[T], options: InsertManyOptions) =
    executor.executeAsync(
      WriteOperation(
        namespace,
        documents.map(doc => new InsertRequest(BsonUtil.toBson(doc))),
        options.isOrdered,
        writeConcern)).toList.toBlocking.toFuture.map(_ => ())

  override def delete[T: Codec](filter: T, multi: Boolean) =
    executeWrite[DeleteResult](new DeleteRequest(BsonUtil.toBson(filter)))

  override def update[T: Codec](
    filter: T,
    update: T,
    upsert: Boolean = false,
    multi: Boolean = false) =
    executeWrite[UpdateResult[T]](
      new UpdateRequest(BsonUtil.toBson(filter), BsonUtil.toBson(update), WriteRequest.Type.UPDATE)
        .upsert(upsert)
        .multi(multi))

  override def upsert[T: Codec](
    filter: T,
    update: T,
    multi: Boolean = false) = this.update(filter, update, upsert = true, multi = multi)

  // FIXME: When update is not valid document (eg.: instead of { $set : { field: value } } it is { field: value } the
  // weird exception is thrown by the driver...

  override def findAndModify[T: Codec](update: T) =
    FindAndModifyBuilder(
      update = update,
      executor = executor,
      namespace = namespace)

  override def findAndModify[T: Codec](filter: T, update: T) =
    FindAndModifyBuilder(
      filter = Some(filter),
      update = update,
      executor = executor,
      namespace = namespace)

  override def drop() =
    executor.executeAsync(new DropCollectionOperation(namespace))
      .toBlocking.toFuture.map(_ => ())

  private def buildIndexRequests[T: Codec](indexes: List[IndexModel[T]]) =
    indexes.foldRight(List.empty[IndexRequest])(
      (model, requests) => requests :+ model.asIndexRequest())

  override def createIndex[T: Codec](key: T, options: IndexOptions) =
    createIndexes(List(IndexModel(key, options)))

  override def createIndexes[T: Codec](indexes: List[IndexModel[T]]) =
    executor.executeAsync(CreateIndexesOperation(namespace, buildIndexRequests(indexes)))
      .toBlocking.toFuture.map(_ => ())

  override def listIndexes() =
    ListIndexesCursor(namespace, readPreference, executor = executor)

  override def dropIndex(indexName: String) =
    executor.executeAsync(DropIndexOperation(namespace, indexName))
      .toBlocking.toFuture.map(_ => ())

  override def dropIndex[T: Codec](keys: T) =
    executor.executeAsync(DropIndexOperation(namespace, BsonUtil.toBson(keys)))
      .toBlocking.toFuture.map(_ => ())

  override def dropIndexes() =
    dropIndex("*")

  override def renameCollection(newCollectionNamespace: MongoNamespace, options: RenameCollectionOptions) =
    executor.executeAsync(new RenameCollectionOperation(namespace, newCollectionNamespace)
      .dropTarget(options.isDropTarget))
      .toBlocking.toFuture.map(_ => ())

  private def executeWrite[T](request: WriteRequest)(implicit f: BulkWriteResult => T) =
    executeSingleWriteRequest(request).map(f)

  private def executeSingleWriteRequest(request: WriteRequest): Future[BulkWriteResult] =
    executor.executeAsync[BulkWriteResult](
      WriteOperation(namespace, List(request), ordered = true, writeConcern))
      .toBlocking.toFuture
}
