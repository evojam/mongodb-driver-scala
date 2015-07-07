package com.evojam.mongodb.client

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

import com.mongodb._
import com.mongodb.bulk.{DeleteRequest, WriteRequest, UpdateRequest, InsertRequest, IndexRequest, BulkWriteResult => MongoBulkWriteResult} // scalastyle:ignore
import com.mongodb.client.model._
import com.mongodb.client.result.{DeleteResult => MongoDeleteResult}
import com.mongodb.operation._
import org.bson.codecs.Codec
import rx.lang.scala.Observable

import com.evojam.mongodb.client.builder.{FindAndRemoveBuilder, FindAndUpdateBuilder}
import com.evojam.mongodb.client.cursor._
import com.evojam.mongodb.client.model.{IndexModel, WriteOperation}
import com.evojam.mongodb.client.model.bulk.{Delete, Update, Replace, Insert, WriteModel}
import com.evojam.mongodb.client.model.operation.{CountOperation, CreateIndexesOperation, DropIndexOperation}
import com.evojam.mongodb.client.model.options.{FindOptions, MapReduceOptions}
import com.evojam.mongodb.client.model.result.{BulkWriteResult, UpdateResult}
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

  override def count[T: Codec](filter: T, options: CountOptions)(implicit exc: ExecutionContext) =
    executor.executeAsync(
      CountOperation(namespace, Option(filter), options), readPreference)
      .map(_.longValue)
      .toBlocking.toFuture

  override def find[T: Codec](filter: T) =
    FindCursor(Option(filter), FindOptions(), namespace, readPreference, executor)

  override def distinct[T: Codec](fieldName: String, filter: T) =
    DistinctCursor(fieldName, Option(filter), namespace, readPreference, executor)

  override def aggregate[T: Codec](pipeline: List[T]) =
    AggregateCursor(pipeline, namespace, readPreference, executor)

  override def mapReduce[T: Codec](mapFunction: String, reduceFunction: String) =
    MapReduceCursor(mapFunction, reduceFunction, readPreference, namespace, executor, MapReduceOptions[T]())

  override def bulkWrite(requests: List[WriteModel], ordered: Boolean)(implicit exc: ExecutionContext) =
    executor.executeAsync(new MixedBulkWriteOperation(namespace, writeRequests(requests), ordered, writeConcern))
      .map(BulkWriteResult.mongo2BulkWriteResult)
      .toBlocking.toFuture

  private def writeRequests(requests: List[WriteModel]) = requests.map {
    case insert @ Insert(_) =>
      new InsertRequest(insert.documentBson)
    case replace @ Replace(_, _, upsert) =>
      new UpdateRequest(replace.filterBson, replace.replaceBson, WriteRequest.Type.REPLACE)
        .upsert(upsert)
    case update @ Update(_, _, upsert, multi) =>
      new UpdateRequest(update.filterBson, update.updateBson, WriteRequest.Type.UPDATE)
        .upsert(upsert)
        .multi(multi)
    case delete @ Delete(_, multi) =>
      new DeleteRequest(delete.filterBson)
        .multi(multi)
  }

  override protected def rawInsert[T: Codec](document: T)(implicit exc: ExecutionContext) =
    executeWrite(new InsertRequest(BsonUtil.toBson(document)))(_ => (), exc)
      .toBlocking.toFuture

  override protected def rawInsertAll[T: Codec](documents: List[T], options: InsertManyOptions)
    (implicit exc: ExecutionContext) =
    executor.executeAsync(
      WriteOperation(
        namespace,
        documents.map(doc => new InsertRequest(BsonUtil.toBson(doc))),
        options.isOrdered,
        writeConcern))
      .toList.map(_ => ())
      .toBlocking.toFuture

  override def delete[T: Codec](filter: T, multi: Boolean)(implicit exc: ExecutionContext) =
    executeWrite[MongoDeleteResult](new DeleteRequest(BsonUtil.toBson(filter)))
      .toBlocking.toFuture

  override def update[T: Codec](
    filter: T,
    update: T,
    upsert: Boolean = false,
    multi: Boolean = false)(implicit exc: ExecutionContext) =
    executeWrite[UpdateResult[T]](
      new UpdateRequest(BsonUtil.toBson(filter), BsonUtil.toBson(update), WriteRequest.Type.UPDATE)
        .upsert(upsert)
        .multi(multi))
      .toBlocking.toFuture

  override def upsert[T: Codec](
    filter: T,
    update: T,
    multi: Boolean = false)(implicit exc: ExecutionContext) =
    this.update(filter, update, upsert = true, multi = multi)

  // FIXME: When update is not valid document (eg.: instead of { $set : { field: value } } it is { field: value } the
  // weird exception is thrown by the driver...

  override def findAndUpdate[T: Codec](update: T) =
    FindAndUpdateBuilder(
      update = update,
      executor = executor,
      namespace = namespace)

  override def findAndUpdate[T: Codec](filter: T, update: T) =
    FindAndUpdateBuilder(
      filter = Some(filter),
      update = update,
      executor = executor,
      namespace = namespace)

  override def findAndRemove[T: Codec](filter: T) =
    FindAndRemoveBuilder(
      filter = filter,
      executor = executor,
      namespace = namespace)

  override def drop()(implicit exc: ExecutionContext) =
    executor.executeAsync(new DropCollectionOperation(namespace))
      .map(_ => ())
      .toBlocking.toFuture

  private def buildIndexRequests[T: Codec](indexes: List[IndexModel[T]]) =
    indexes.foldRight(List.empty[IndexRequest])(
      (model, requests) => requests :+ model.asIndexRequest())

  override def createIndex[T: Codec](key: T, options: IndexOptions)(implicit exc: ExecutionContext) =
    createIndexes(List(IndexModel(key, options)))

  override def createIndexes[T: Codec](indexes: List[IndexModel[T]])(implicit exc: ExecutionContext) =
    executor.executeAsync(CreateIndexesOperation(namespace, buildIndexRequests(indexes)))
      .map(_ => ())
      .toBlocking.toFuture

  override def listIndexes() =
    ListIndexesCursor(namespace, readPreference, executor = executor)

  override def dropIndex(indexName: String)(implicit exc: ExecutionContext) =
    executor.executeAsync(DropIndexOperation(namespace, indexName))
      .map(_ => ())
      .toBlocking.toFuture

  override def dropIndex[T: Codec](keys: T)(implicit exc: ExecutionContext) =
    executor.executeAsync(DropIndexOperation(namespace, BsonUtil.toBson(keys)))
      .map(_ => ())
      .toBlocking.toFuture

  override def dropIndexes()(implicit exc: ExecutionContext) =
    dropIndex("*")

  override def renameCollection(newCollectionNamespace: MongoNamespace, options: RenameCollectionOptions)
    (implicit exc: ExecutionContext) =
    executor.executeAsync(new RenameCollectionOperation(namespace, newCollectionNamespace)
      .dropTarget(options.isDropTarget))
      .map(_ => ())
      .toBlocking.toFuture

  private def executeWrite[T](request: WriteRequest)
    (implicit f: MongoBulkWriteResult => T, exc: ExecutionContext) =
    executeSingleWriteRequest(request).map(f)

  private def executeSingleWriteRequest(request: WriteRequest)
    (implicit exc: ExecutionContext): Observable[MongoBulkWriteResult] =
    executor.executeAsync[MongoBulkWriteResult](
      WriteOperation(namespace, List(request), ordered = true, writeConcern))
}
