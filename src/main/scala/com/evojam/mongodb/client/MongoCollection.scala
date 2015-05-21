package com.evojam.mongodb.client

import scala.collection.JavaConversions.seqAsJavaList
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import com.mongodb.bulk.IndexRequest
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.bulk.DeleteRequest
import com.mongodb.bulk.InsertRequest
import com.mongodb.bulk.UpdateRequest
import com.mongodb.bulk.WriteRequest
import com.mongodb.client.model._
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.bson.BsonDocument
import org.bson.BsonDocumentWrapper
import org.bson.Document
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

import com.evojam.mongodb.client.iterable.DistinctIterable
import com.evojam.mongodb.client.iterable.FindIterable
import com.evojam.mongodb.client.iterable.ListIndexesIterable
import com.evojam.mongodb.client.model.IndexModel
import com.evojam.mongodb.client.model.WriteOperation
import com.evojam.mongodb.client.model.operation.CountOperation
import com.evojam.mongodb.client.model.operation.CreateIndexesOperation
import com.evojam.mongodb.client.model.options.FindOptions
import com.evojam.mongodb.client.util.BsonUtil
import com.evojam.mongodb.client.util.Conversions._

case class MongoCollection[TDoc <: Any : Manifest](//scalastyle:ignore
  namespace: MongoNamespace,
  implicit val codecRegistry: CodecRegistry,
  implicit val readPreference: ReadPreference,
  implicit val writeConcern: WriteConcern,
  private val executor: ObservableOperationExecutor) {

  require(namespace != null, "namespace cannot be null")
  require(codecRegistry != null, "codecRegistry cannot be null")
  require(readPreference != null, "readPreference cannot be null")
  require(writeConcern != null, "writeConcern cannot be null")
  require(executor != null, "executor cannot be null")

  implicit val documentClass = manifest[TDoc].runtimeClass.asInstanceOf[Class[TDoc]]

  private val codec = codecRegistry.get(documentClass)

  def withCodecRegistry(codecRegistry: CodecRegistry): MongoCollection[TDoc] =
    this.copy(codecRegistry = codecRegistry)

  def withReadPreference(readPreference: ReadPreference): MongoCollection[TDoc] =
    this.copy(readPreference = readPreference)

  def withWriteConcern(writeConcern: WriteConcern): MongoCollection[TDoc] =
    this.copy(writeConcern = writeConcern)

  def count(): Future[Long] =
    count(None, new CountOptions())

  def count(filter: Bson): Future[Long] =
    count(Some(filter), new CountOptions())

  def count(filter: Option[Bson], options: CountOptions): Future[Long] =
    executor.executeAsync(
      CountOperation(
        namespace,
        filter.map(BsonUtil.toBsonDocument).getOrElse(new BsonDocument),
        options),
      readPreference).toBlocking.toFuture.map(_.longValue)

  def find[TRes <: Any : Manifest](
    filter: Bson = new BsonDocument()): FindIterable[TDoc, TRes] =
    FindIterable[TDoc, TRes](filter, FindOptions(), namespace,
      readPreference, codecRegistry, executor)

  def distinct[TRes <: Any : Manifest](
    fieldName: String,
    filter: Bson = new BsonDocument): DistinctIterable[TDoc, TRes] =
    DistinctIterable[TDoc, TRes](fieldName, filter, namespace,
      readPreference, codecRegistry, executor)

  // TODO: Aggregate
  // TODO: MapReduce
  // TODO: Bulk write/read

  def insertOne(document: TDoc): Future[Unit] =
    executeWrite(new InsertRequest(addIdIfAbsent(document)))(_ => ())

  def insertMany(
    documents: List[TDoc],
    options: InsertManyOptions = new InsertManyOptions): Future[Unit] =
    executor.executeAsync(
      WriteOperation(
        namespace,
        documents.map(doc => new InsertRequest(addIdIfAbsent(doc))),
        options.isOrdered,
        writeConcern)).toList.toBlocking.toFuture.map(_ => ())

  def deleteOne(filter: Bson) = delete(filter, false)

  def deleteMany(filter: Bson) = delete(filter, true)

  def deleteAll() = delete(new Document(), true)

  // TODO: should return Future[DeleteResult]
  def delete(filter: Bson, multi: Boolean): Future[DeleteResult] =
    executeWrite[DeleteResult](new DeleteRequest(filter))

  def replaceOne(filter: Bson, replacement: TDoc): Future[UpdateResult] = ???

  def replaceOne(filter: Bson, replacement: TDoc, options: UpdateOptions): Future[UpdateResult] = ???

  def updateOne(
    filterBson: Bson,
    updateBson: Bson,
    options: UpdateOptions = new UpdateOptions) =
    update(filterBson, updateBson, options, false)

  def updateMany(filterBson: Bson, updateBson: Bson, options: UpdateOptions) =
    update(filterBson, updateBson, options, true)

  def update(filterBson: Bson, updateBson: Bson, options: UpdateOptions, multi: Boolean): Future[UpdateResult] =
    executeWrite[UpdateResult](
      new UpdateRequest(filterBson, updateBson, WriteRequest.Type.UPDATE)
        .upsert(options.isUpsert)
        .multi(multi))

  def findOneAndDelete(filter: Bson): Future[TDoc] = ???

  def findOneAndDelete(filter: Bson, options: FindOneAndDeleteOptions): Future[TDoc] = ???

  def findOneAndReplace(filter: Bson, replacement: TDoc): Future[TDoc] = ???

  def findOneAndReplace(filter: Bson, replacement: TDoc, options: FindOneAndReplaceOptions): Future[TDoc] = ???

  def findOneAndUpdate(filter: Bson, update: Bson): Future[TDoc] = ???

  def findOneAndUpdate(filter: Bson, update: Bson, options: FindOneAndUpdateOptions): Future[TDoc] = ???

  def drop(): Future[Unit] = ???

  private def buildIndexRequests(indexes: List[IndexModel]) =
    indexes.foldRight(List.empty[IndexRequest])(
      (model, requests) => requests :+ model.asIndexRequest())

  def createIndex(key: Bson): Future[Unit] =
    createIndex(key, new IndexOptions())

  def createIndex(key: Bson, options: IndexOptions): Future[Unit] =
    createIndexes(List(IndexModel(key, options)))

  def createIndexes(indexes: List[IndexModel]): Future[Unit] =
      executor.executeAsync(CreateIndexesOperation(namespace, buildIndexRequests(indexes)))
        .toBlocking.toFuture
        .map(_ => ())

  def listIndexes[TRes <: Any : Manifest](): ListIndexesIterable[TDoc, TRes] =
    ListIndexesIterable[TDoc, TRes](namespace, readPreference, codecRegistry, executor = executor)

  def dropIndex(indexName: String): Future[Unit] = ???

  def dropIndex(keys: Bson): Future[Unit] = ???

  def dropIndexes(): Future[Unit] = ???

  def renameCollection(newCollectionNamespace: MongoNamespace): Future[Unit] = ???

  def renameCollection(newCollectionNamespace: MongoNamespace, options: RenameCollectionOptions): Future[Unit] = ???

  private def executeWrite[T](request: WriteRequest)(implicit f: BulkWriteResult => T) =
    executeSingleWriteRequest(request).map(f)

  private def executeSingleWriteRequest(request: WriteRequest): Future[BulkWriteResult] =
    executor.executeAsync[BulkWriteResult](
      WriteOperation(namespace, List(request), true, writeConcern))
      .toBlocking.toFuture

  private implicit def documentToBsonDocument(doc: TDoc): BsonDocument =
    BsonDocumentWrapper.asBsonDocument(doc, codecRegistry)

  private implicit def bsonToBsonDocument(bson: Bson): BsonDocument =
    if (bson == null) {
      null
    } else {
      bson.toBsonDocument(documentClass, codecRegistry)
    }

  private def addIdIfAbsent(doc: TDoc) = codec match {
    case collCodec: CollectibleCodec[TDoc] =>
      collCodec.generateIdIfAbsentFromDocument(doc)
      doc
    case _ => doc
  }
}
