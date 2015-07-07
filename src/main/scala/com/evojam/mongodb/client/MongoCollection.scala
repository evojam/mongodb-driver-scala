package com.evojam.mongodb.client

import scala.concurrent.{ExecutionContext, Future}

import com.mongodb.client.model._
import com.mongodb.client.result.DeleteResult
import com.mongodb.{MongoNamespace, ReadPreference, WriteConcern}
import org.bson.BsonDocument
import org.bson.codecs.Codec

import com.evojam.mongodb.client.builder.{FindAndRemoveBuilder, FindAndUpdateBuilder}
import com.evojam.mongodb.client.codec.Writer
import com.evojam.mongodb.client.cursor._
import com.evojam.mongodb.client.model.IndexModel
import com.evojam.mongodb.client.model.bulk.WriteModel
import com.evojam.mongodb.client.model.result.{BulkWriteResult, UpdateResult}

trait MongoCollection {
  def withReadPreference(readPreference: ReadPreference): MongoCollection

  def withWriteConcern(writeConcern: WriteConcern): MongoCollection

  def count()(implicit exc: ExecutionContext): Future[Long] =
    count[BsonDocument](new BsonDocument(), new CountOptions())(bsonDocumentCodec, exc)

  def count[T: Codec](filter: T)(implicit exc: ExecutionContext): Future[Long] =
    count(filter, new CountOptions())

  def count[T: Codec](filter: T, options: CountOptions)(implicit exc: ExecutionContext): Future[Long]

  def find(): FindCursor[BsonDocument] =
    find[BsonDocument](new BsonDocument())(bsonDocumentCodec)

  def find[T: Codec](filter: T): FindCursor[T]

  def distinct(fieldName: String): DistinctCursor[BsonDocument] =
    distinct[BsonDocument](fieldName, new BsonDocument())(bsonDocumentCodec)

  def distinct[T: Codec](fieldName: String, filter: T): DistinctCursor[T]

  def aggregate[T: Codec](pipeline: List[T]): AggregateCursor[T]

  protected def rawInsert[T: Codec](document: T)(implicit exc: ExecutionContext): Future[Unit]

  protected def rawInsertAll[T: Codec](
    documents: List[T],
    options: InsertManyOptions = new InsertManyOptions())(implicit exc: ExecutionContext): Future[Unit]

  def insert[T](document: T)(implicit w: Writer[T], exc: ExecutionContext): Future[Unit] =
    rawInsert(w.write(document))(w.codec, exc)

  def insertAll[T](
    documents: List[T],
    options: InsertManyOptions = new InsertManyOptions())(implicit w: Writer[T], exc: ExecutionContext): Future[Unit] =
    rawInsertAll(documents.map(w.write(_)), options)(w.codec, exc)

  def mapReduce[T: Codec](
    mapFunction: String,
    reduceFunction: String): MapReduceCursor[T]

  def bulkWrite(requests: List[WriteModel], ordered: Boolean = true)
    (implicit exc: ExecutionContext): Future[BulkWriteResult]

  def delete[T: Codec](filter: T, multi: Boolean = false)(implicit exc: ExecutionContext): Future[DeleteResult]

  def update[T: Codec](
    filter: T,
    update: T,
    upsert: Boolean = false,
    multi: Boolean = false)(implicit exc: ExecutionContext): Future[UpdateResult[T]]

  def upsert[T: Codec](
    filter: T,
    update: T,
    multi: Boolean = false)(implicit exc: ExecutionContext): Future[UpdateResult[T]]

  def findAndUpdate[T: Codec](update: T): FindAndUpdateBuilder[T]

  def findAndUpdate[T: Codec](filter: T, update: T): FindAndUpdateBuilder[T]

  def findAndRemove[T: Codec](filter: T): FindAndRemoveBuilder[T]

  def drop()(implicit exc: ExecutionContext): Future[Unit]

  def createIndex[T: Codec](key: T, options: IndexOptions = new IndexOptions())
    (implicit exc: ExecutionContext): Future[Unit]

  def createIndexes[T: Codec](indexes: List[IndexModel[T]])(implicit exc: ExecutionContext): Future[Unit]

  def listIndexes(): ListIndexesCursor

  def dropIndex(indexName: String)(implicit exc: ExecutionContext): Future[Unit]

  def dropIndex[T: Codec](keys: T)(implicit exc: ExecutionContext): Future[Unit]

  def dropIndexes()(implicit exc: ExecutionContext): Future[Unit]

  def renameCollection(
    newCollectionNamespace: MongoNamespace,
    options: RenameCollectionOptions = new RenameCollectionOptions())(implicit exc: ExecutionContext): Future[Unit]
}
