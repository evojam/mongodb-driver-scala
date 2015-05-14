package com.evojam.mongodb.client

import scala.concurrent.Future

import com.mongodb.client.model._
import com.mongodb.client.result.{ UpdateResult, DeleteResult }
import com.mongodb.{ MongoNamespace, WriteConcern, ReadPreference }
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import rx.lang.scala.Observable

import com.evojam.mongodb.client.iterable.DistinctIterable
import com.evojam.mongodb.client.iterable.ListIndexesIterable

trait MongoCollection[T] {

  def withDocumentClass[U](newDocumentClass: Class[U]): MongoCollection[U]
  def withCodecRegistry(codecRegistry: CodecRegistry): MongoCollection[T]
  def withReadPreference(readPreference: ReadPreference): MongoCollection[T]
  def withWriteConcern(writeConcern: WriteConcern): MongoCollection[T]

  def count(): Future[Long]
  def count(filter: Bson): Future[Long]
  def count(filter: Bson, options: CountOptions): Future[Long]

  def distinct[U](filedName: String, resultClass: Class[U]): DistinctIterable[U]

  def find(): Observable[T]
  def find[U](resultClass: Class[U]): Observable[U]
  def find(filter: Bson): Observable[T]
  def find[U](filter: Bson, resultClass: Class[U]): Observable[U]

  // TODO: Aggregate
  // TODO: MapReduce
  // TODO: Bulk write/read

  def insertOne(document: T): Future[Unit]
  def insertMany(documents: Iterable[T]): Future[Unit]
  def insertMany(documents: Iterable[T], options: InsertManyOptions): Future[Unit]

  def deleteOne(filter: Bson): Future[DeleteResult]
  def deleteMany(filter: Bson): Future[DeleteResult]

  def replaceOne(filter: Bson, replacement: T): Future[UpdateResult]
  def replaceOne(filter: Bson, replacement: T, options: UpdateOptions): Future[UpdateResult]

  def updateOne(filter: Bson, update: Bson): Future[UpdateResult]
  def updateOne(filter: Bson, update: Bson, options: UpdateOptions): Future[UpdateResult]
  def updateMany(filter: Bson, update: Bson, options: UpdateOptions): Future[UpdateResult]

  def findOneAndDelete(filter: Bson): Future[T]
  def findOneAndDelete(filter: Bson, options: FindOneAndDeleteOptions): Future[T]

  def findOneAndReplace(filter: Bson, replacement: T): Future[T]
  def findOneAndReplace(filter: Bson, replacement: T, options: FindOneAndReplaceOptions): Future[T]

  def findOneAndUpdate(filter: Bson, update: Bson): Future[T]
  def findOneAndUpdate(filter: Bson, update: Bson, options: FindOneAndUpdateOptions): Future[T]

  def drop(): Future[Unit]

  def createIndex(key: Bson): Future[String]
  def createIndex(key: Bson, options: IndexOptions): Future[String]
  def createIndex(indexes: Iterable[IndexModel]): Future[String]

  def listIndexes(): ListIndexesIterable[T]
  def listIndexes[U](resultClass: Class[U]): ListIndexesIterable[T]

  def dropIndex(indexName: String): Future[Unit]
  def dropIndex(keys: Bson): Future[Unit]
  def dropIndexes(): Future[Unit]

  def renameCollection(newCollectionNamespace: MongoNamespace): Future[Unit]
  def renameCollection(newCollectionNamespace: MongoNamespace, options: RenameCollectionOptions): Future[Unit]
}
