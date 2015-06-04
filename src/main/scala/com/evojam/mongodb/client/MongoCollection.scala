package com.evojam.mongodb.client

import scala.concurrent.Future

import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.RenameCollectionOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.bson.BsonDocument
import org.bson.codecs.Codec

import com.evojam.mongodb.client.codec.Codecs
import com.evojam.mongodb.client.iterable.DistinctIterable
import com.evojam.mongodb.client.iterable.FindIterable
import com.evojam.mongodb.client.iterable.ListIndexesIterable
import com.evojam.mongodb.client.model.IndexModel

trait MongoCollection {
  def withReadPreference(readPreference: ReadPreference): MongoCollection

  def withWriteConcern(writeConcern: WriteConcern): MongoCollection

  def count(): Future[Long] =
    count[BsonDocument](new BsonDocument(), new CountOptions())(Codecs.bsonDocumentCodec)

  def count[T: Codec](filter: T): Future[Long] =
    count(filter, new CountOptions())

  def count[T: Codec](filter: T, options: CountOptions): Future[Long]

  def find(): FindIterable[BsonDocument] =
    find[BsonDocument](new BsonDocument())(Codecs.bsonDocumentCodec)

  def find[T: Codec](filter: T): FindIterable[T]

  def distinct(fieldName: String): DistinctIterable[BsonDocument] =
    distinct[BsonDocument](fieldName, new BsonDocument())(Codecs.bsonDocumentCodec)

  def distinct[T: Codec](fieldName: String, filter: T): DistinctIterable[T]

  def insert[T: Codec](document: T): Future[Unit]

  def insert[T: Codec](documents: List[T], options: InsertManyOptions = new InsertManyOptions()): Future[Unit]

  def delete[T: Codec](filter: T, multi: Boolean = false): Future[DeleteResult]

  def update[T: Codec](
    filterBson: T,
    updateBson: T,
    options: UpdateOptions = new UpdateOptions(),
    multi: Boolean = false): Future[UpdateResult]

  def drop(): Future[Unit]

  def createIndex[T: Codec](
    key: T,
    options: IndexOptions = new IndexOptions()): Future[Unit]

  def createIndexes[T: Codec](indexes: List[IndexModel[T]]): Future[Unit]

  def listIndexes(): ListIndexesIterable

  def dropIndex(indexName: String): Future[Unit]

  def dropIndex[T: Codec](keys: T): Future[Unit]

  def dropIndexes(): Future[Unit]

  def renameCollection(
    newCollectionNamespace: MongoNamespace,
    options: RenameCollectionOptions = new RenameCollectionOptions()): Future[Unit]
}
