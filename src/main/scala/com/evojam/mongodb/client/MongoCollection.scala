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
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

import com.evojam.mongodb.client.iterable.DistinctIterable
import com.evojam.mongodb.client.iterable.FindIterable
import com.evojam.mongodb.client.iterable.ListIndexesIterable
import com.evojam.mongodb.client.model.IndexModel

trait MongoCollection[TDoc] { // scalastyle:ignore
  def withCodecRegistry(codecRegistry: CodecRegistry): MongoCollection[TDoc]

  def withReadPreference(readPreference: ReadPreference): MongoCollection[TDoc]

  def withWriteConcern(writeConcern: WriteConcern): MongoCollection[TDoc]

  def count(
      filter: Bson = new BsonDocument(),
      options: CountOptions = new CountOptions()): Future[Long]

  def find(filter: Bson = new BsonDocument()): FindIterable[TDoc, Document]

  def findOfType[TRes <: Any : Manifest](filter: Bson = new BsonDocument): FindIterable[TDoc, TRes]

  def distinct(
    fieldName: String,
    filter: Bson = new BsonDocument()): DistinctIterable[TDoc, Document]

  def distinctOfType[TRes <: Any : Manifest](
    fieldName: String,
    filter: Bson = new BsonDocument()): DistinctIterable[TDoc, TRes]

  def insert(document: TDoc): Future[Unit]

  def insert(
    documents: List[TDoc],
    options: InsertManyOptions = new InsertManyOptions()): Future[Unit]

  def delete(
    filter: Bson = new Document(),
    multi: Boolean = false): Future[DeleteResult]

  def update(
    filterBson: Bson,
    updateBson: Bson,
    options: UpdateOptions = new UpdateOptions(),
    multi: Boolean = false): Future[UpdateResult]

  def drop(): Future[Unit]

  def createIndex(
    key: Bson,
    options: IndexOptions = new IndexOptions()): Future[Unit]

  def createIndexes(indexes: List[IndexModel]): Future[Unit]

  def listIndexes(): ListIndexesIterable[TDoc, Document]

  def listIndexesOfType[TRes <: Any : Manifest](): ListIndexesIterable[TDoc, TRes]

  def dropIndex(indexName: String): Future[Unit]

  def dropIndex(keys: Bson): Future[Unit]

  def dropIndexes(): Future[Unit]

  def renameCollection(
    newCollectionNamespace: MongoNamespace,
    options: RenameCollectionOptions = new RenameCollectionOptions()): Future[Unit]
}
