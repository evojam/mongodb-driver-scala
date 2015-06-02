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
import org.bson.codecs.Codec
import org.bson.conversions.Bson

import com.evojam.mongodb.client.iterable.DistinctIterable
import com.evojam.mongodb.client.iterable.FindIterable
import com.evojam.mongodb.client.iterable.ListIndexesIterable
import com.evojam.mongodb.client.model.IndexModel

trait MongoCollection {
  def withReadPreference(readPreference: ReadPreference): MongoCollection

  def withWriteConcern(writeConcern: WriteConcern): MongoCollection

  def count[T: Codec](filter: T, options: CountOptions = new CountOptions()): Future[Long]

  def find[T: Codec](filter: T): FindIterable[T]

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

  def dropIndex(keys: Bson): Future[Unit]

  def dropIndexes(): Future[Unit]

  def renameCollection(
    newCollectionNamespace: MongoNamespace,
    options: RenameCollectionOptions = new RenameCollectionOptions()): Future[Unit]
}
