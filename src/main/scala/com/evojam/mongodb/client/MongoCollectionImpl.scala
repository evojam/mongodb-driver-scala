package com.evojam.mongodb.client

import javafx.beans.binding.When

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

import com.evojam.mongodb.client.codec.Reader
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
import com.mongodb.operation.{ FindAndUpdateOperation, DropCollectionOperation, RenameCollectionOperation }
import org.bson.{ BsonDocument, Document }
import org.bson.codecs.{ BsonDocumentCodec, DocumentCodec, Codec, CollectibleCodec }

import com.evojam.mongodb.client.iterable.AggregateIterable
import com.evojam.mongodb.client.iterable.DistinctIterable
import com.evojam.mongodb.client.iterable.FindIterable
import com.evojam.mongodb.client.iterable.ListIndexesIterable
import com.evojam.mongodb.client.model.IndexModel
import com.evojam.mongodb.client.model.WriteOperation
import com.evojam.mongodb.client.model.operation.CountOperation
import com.evojam.mongodb.client.model.operation.CreateIndexesOperation
import com.evojam.mongodb.client.model.operation.DropIndexOperation
import com.evojam.mongodb.client.model.options.FindOptions
import com.evojam.mongodb.client.util.BsonUtil
import com.evojam.mongodb.client.util.Conversions._

case class MongoCollectionImpl(
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
    FindIterable(Option(filter), FindOptions(), namespace, readPreference, executor)

  override def distinct[T: Codec](fieldName: String, filter: T) =
    DistinctIterable(fieldName, Option(filter), namespace, readPreference, executor)

  override def aggregate[T: Codec](pipeline: List[T]) =
    AggregateIterable(pipeline, namespace, readPreference, executor)

  // TODO: MapReduce
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

  override def update[T: Codec](filter: T, update: T, upsert: Boolean, multi: Boolean) =
    executeWrite[UpdateResult](
      new UpdateRequest(BsonUtil.toBson(filter), BsonUtil.toBson(update), WriteRequest.Type.UPDATE)
        .upsert(upsert)
        .multi(multi))

  override def upsert[T: Codec](
    filter: T,
    update: T,
    multi: Boolean = false) = this.update(filter, update, upsert = true, multi = multi)

  // TODO: Implement sort
  // FIXME: When update is not valid document (eg.: instead of { $set : { field: value } } it is { field: value } the
  // weird exception is thrown by the driver...

  def executeFindAndModify[T, R](filter: T, update: T, returnFormer: Boolean, upsert: Boolean)(implicit codec: Codec[T],
    reader: Reader[R]): Future[Option[R]] =
    executor.executeAsync(
      new FindAndUpdateOperation(namespace, reader.codec, BsonUtil.toBson(update))
        .upsert(upsert).returnOriginal(returnFormer))
      .toBlocking.toFuture.map(Option(_).map(reader.read))

  override def findAndModify[T: Codec, R: Reader](
    filter: T,
    update: T,
    returnFormer: Boolean = false,
    upsert: Boolean = false) =
    executeFindAndModify(filter, update, returnFormer, upsert)

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
    ListIndexesIterable(namespace, readPreference, executor = executor)

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
