package com.evojam.mongodb.client

import scala.concurrent.Future

import com.mongodb.client.model._
import com.mongodb.client.result.DeleteResult
import com.mongodb.{MongoNamespace, ReadPreference, WriteConcern}
import org.bson.BsonDocument
import org.bson.codecs.Codec

import com.evojam.mongodb.client.builder.FindAndModifyBuilder
import com.evojam.mongodb.client.codec.{Codecs, Writer}
import com.evojam.mongodb.client.cursor._
import com.evojam.mongodb.client.model.IndexModel
import com.evojam.mongodb.client.model.result.UpdateResult

trait MongoCollection {
  def withReadPreference(readPreference: ReadPreference): MongoCollection

  def withWriteConcern(writeConcern: WriteConcern): MongoCollection

  def count(): Future[Long] =
    count[BsonDocument](new BsonDocument(), new CountOptions())(Codecs.bsonDocumentCodec)

  def count[T: Codec](filter: T): Future[Long] =
    count(filter, new CountOptions())

  def count[T: Codec](filter: T, options: CountOptions): Future[Long]

  def find(): FindCursor[BsonDocument] =
    find[BsonDocument](new BsonDocument())(Codecs.bsonDocumentCodec)

  def find[T: Codec](filter: T): FindCursor[T]

  def distinct(fieldName: String): DistinctCursor[BsonDocument] =
    distinct[BsonDocument](fieldName, new BsonDocument())(Codecs.bsonDocumentCodec)

  def distinct[T: Codec](fieldName: String, filter: T): DistinctCursor[T]

  def aggregate[T: Codec](pipeline: List[T]): AggregateCursor[T]

  protected def rawInsert[T: Codec](document: T): Future[Unit]

  protected def rawInsertAll[T: Codec](
    documents: List[T],
    options: InsertManyOptions = new InsertManyOptions()): Future[Unit]

  def insert[T](document: T)(implicit w: Writer[T]): Future[Unit] =
    rawInsert(w.write(document))(w.codec)

  def insertAll[T](
    documents: List[T],
    options: InsertManyOptions = new InsertManyOptions())(implicit w: Writer[T]): Future[Unit] =
    rawInsertAll(documents.map(w.write(_)), options)(w.codec)

  def mapReduce[T: Codec](
    mapFunction: String,
    reduceFunction: String): MapReduceCursor[T]

  def delete[T: Codec](filter: T, multi: Boolean = false): Future[DeleteResult]

  def update[T: Codec](
    filter: T,
    update: T,
    upsert: Boolean = false,
    multi: Boolean = false): Future[UpdateResult[T]]

  def upsert[T: Codec](
    filter: T,
    update: T,
    multi: Boolean = false): Future[UpdateResult[T]]

  def findAndModify[T: Codec](update: T): FindAndModifyBuilder[T]

  def findAndModify[T: Codec](filter: T, update: T): FindAndModifyBuilder[T]

  def drop(): Future[Unit]

  def createIndex[T: Codec](key: T, options: IndexOptions = new IndexOptions()): Future[Unit]

  def createIndexes[T: Codec](indexes: List[IndexModel[T]]): Future[Unit]

  def listIndexes(): ListIndexesCursor

  def dropIndex(indexName: String): Future[Unit]

  def dropIndex[T: Codec](keys: T): Future[Unit]

  def dropIndexes(): Future[Unit]

  def renameCollection(
    newCollectionNamespace: MongoNamespace,
    options: RenameCollectionOptions = new RenameCollectionOptions()): Future[Unit]
}
