package com.evojam.mongodb.client

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.bulk.DeleteRequest
import com.mongodb.bulk.IndexRequest
import com.mongodb.bulk.InsertRequest
import com.mongodb.bulk.UpdateRequest
import com.mongodb.bulk.WriteRequest
import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.RenameCollectionOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.operation.DropCollectionOperation
import com.mongodb.operation.RenameCollectionOperation
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

case class MongoCollectionImpl[TDoc <: Any : Manifest](//scalastyle:ignore
  namespace: MongoNamespace,
  implicit val codecRegistry: CodecRegistry,
  implicit val readPreference: ReadPreference,
  implicit val writeConcern: WriteConcern,
  private val executor: ObservableOperationExecutor) extends MongoCollection[TDoc] {

  require(namespace != null, "namespace cannot be null")
  require(codecRegistry != null, "codecRegistry cannot be null")
  require(readPreference != null, "readPreference cannot be null")
  require(writeConcern != null, "writeConcern cannot be null")
  require(executor != null, "executor cannot be null")

  implicit val documentClass = manifest[TDoc].runtimeClass.asInstanceOf[Class[TDoc]]

  private val codec = codecRegistry.get(documentClass)

  override def withCodecRegistry(codecRegistry: CodecRegistry) =
    this.copy(codecRegistry = codecRegistry)

  override def withReadPreference(readPreference: ReadPreference) =
    this.copy(readPreference = readPreference)

  override def withWriteConcern(writeConcern: WriteConcern) =
    this.copy(writeConcern = writeConcern)

  override def count(filter: Bson, options: CountOptions) =
    executor.executeAsync(
      CountOperation(
        namespace,
        BsonUtil.toBsonDocument(filter),
        options),
      readPreference).toBlocking.toFuture.map(_.longValue)

  override def find(filter: Bson) =
    find[Document](filter)

  override def find[TRes <: Any : Manifest](filter: Bson) =
    FindIterable[TDoc, TRes](filter, FindOptions(), namespace,
      readPreference, codecRegistry, executor)

  override def distinct(fieldName: String, filter: Bson) =
    distinct[Document](fieldName, filter)

  override def distinct[TRes <: Any : Manifest](fieldName: String, filter: Bson) =
    DistinctIterable[TDoc, TRes](fieldName, filter, namespace,
      readPreference, codecRegistry, executor)

  // TODO: Aggregate
  // TODO: MapReduce
  // TODO: Bulk write/read

  override def insert(document: TDoc) =
    executeWrite(new InsertRequest(addIdIfAbsent(document)))(_ => ())

  override def insert(documents: List[TDoc], options: InsertManyOptions) =
    executor.executeAsync(
      WriteOperation(
        namespace,
        documents.map(doc => new InsertRequest(addIdIfAbsent(doc))),
        options.isOrdered,
        writeConcern)).toList.toBlocking.toFuture.map(_ => ())

  override def delete(filter: Bson, multi: Boolean) =
    executeWrite[DeleteResult](new DeleteRequest(filter))

  override def update(filterBson: Bson, updateBson: Bson, options: UpdateOptions, multi: Boolean) =
    executeWrite[UpdateResult](
      new UpdateRequest(filterBson, updateBson, WriteRequest.Type.UPDATE)
        .upsert(options.isUpsert)
        .multi(multi))

  override def drop() =
    executor.executeAsync(new DropCollectionOperation(namespace))
      .toBlocking.toFuture.map(_ => ())

  private def buildIndexRequests(indexes: List[IndexModel]) =
    indexes.foldRight(List.empty[IndexRequest])(
      (model, requests) => requests :+ model.asIndexRequest())

  override def createIndex(key: Bson, options: IndexOptions) =
    createIndexes(List(IndexModel(key, options)))

  override def createIndexes(indexes: List[IndexModel]) =
      executor.executeAsync(CreateIndexesOperation(namespace, buildIndexRequests(indexes)))
        .toBlocking.toFuture
        .map(_ => ())

  override def listIndexes =
    listIndexes[Document]()

  override def listIndexes[TRes <: Any : Manifest]() =
    ListIndexesIterable[TDoc, TRes](namespace, readPreference, codecRegistry, executor = executor)

  override def dropIndex(indexName: String) = ???

  override def dropIndex(keys: Bson) = ???

  override def dropIndexes() = ???

  override def renameCollection(newCollectionNamespace: MongoNamespace, options: RenameCollectionOptions) =
    executor.executeAsync(new RenameCollectionOperation(namespace, newCollectionNamespace)
      .dropTarget(options.isDropTarget))
      .toBlocking.toFuture.map(_ => ())

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
