package com.evojam.mongodb.client

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.FindOneAndDeleteOptions
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.RenameCollectionOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.bson.BsonDocument
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import rx.lang.scala.Observable

import com.evojam.mongodb.client.iterable.DistinctIterable
import com.evojam.mongodb.client.iterable.ListIndexesIterable
import com.evojam.mongodb.client.model.CountOperation
import com.evojam.mongodb.client.model.CountOperation.countOperation2Mongo
import com.evojam.mongodb.client.util.BsonUtil

case class MongoCollection[T <: Any : Manifest](
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

  implicit val documentClass = manifest[T].runtimeClass

  def withCodecRegistry(codecRegistry: CodecRegistry): MongoCollection[T] =
    this.copy(codecRegistry = codecRegistry)

  def withReadPreference(readPreference: ReadPreference): MongoCollection[T] =
    this.copy(readPreference = readPreference)

  def withWriteConcern(writeConcern: WriteConcern): MongoCollection[T] =
    this.copy(writeConcern = writeConcern)

  def count(): Future[Long] =
    count(None, new CountOptions())

  def count(filter: Bson): Future[Long] =
    count(Some(filter), new CountOptions())

  def count(filter: Option[Bson], options: CountOptions): Future[Long] =
    executor.execute(
      CountOperation(
        namespace,
        filter.map(BsonUtil.toBsonDocument).getOrElse(new BsonDocument),
        options),
      readPreference).toBlocking.toFuture.map(_.longValue)

  def distinct[U](filedName: String, resultClass: Class[U]): DistinctIterable[U] = ???

  def find(): Observable[T] = ???

  def find[U](resultClass: Class[U]): Observable[U] = ???

  def find(filter: Bson): Observable[T] = ???

  def find[U](filter: Bson, resultClass: Class[U]): Observable[U] = ???

  // TODO: Aggregate
  // TODO: MapReduce
  // TODO: Bulk write/read

  def insertOne(document: T): Future[Unit] = ???

  def insertMany(documents: Iterable[T]): Future[Unit] = ???

  def insertMany(documents: Iterable[T], options: InsertManyOptions): Future[Unit] = ???

  def deleteOne(filter: Bson): Future[DeleteResult] = ???

  def deleteMany(filter: Bson): Future[DeleteResult] = ???

  def replaceOne(filter: Bson, replacement: T): Future[UpdateResult] = ???

  def replaceOne(filter: Bson, replacement: T, options: UpdateOptions): Future[UpdateResult] = ???

  def updateOne(filter: Bson, update: Bson): Future[UpdateResult] = ???

  def updateOne(filter: Bson, update: Bson, options: UpdateOptions): Future[UpdateResult] = ???

  def updateMany(filter: Bson, update: Bson, options: UpdateOptions): Future[UpdateResult] = ???

  def findOneAndDelete(filter: Bson): Future[T] = ???

  def findOneAndDelete(filter: Bson, options: FindOneAndDeleteOptions): Future[T] = ???

  def findOneAndReplace(filter: Bson, replacement: T): Future[T] = ???

  def findOneAndReplace(filter: Bson, replacement: T, options: FindOneAndReplaceOptions): Future[T] = ???

  def findOneAndUpdate(filter: Bson, update: Bson): Future[T] = ???

  def findOneAndUpdate(filter: Bson, update: Bson, options: FindOneAndUpdateOptions): Future[T] = ???

  def drop(): Future[Unit] = ???

  def createIndex(key: Bson): Future[String] = ???

  def createIndex(key: Bson, options: IndexOptions): Future[String] = ???

  def createIndex(indexes: Iterable[IndexModel]): Future[String] = ???

  def listIndexes(): ListIndexesIterable[T] = ???

  def listIndexes[U](resultClass: Class[U]): ListIndexesIterable[T] = ???

  def dropIndex(indexName: String): Future[Unit] = ???

  def dropIndex(keys: Bson): Future[Unit] = ???

  def dropIndexes(): Future[Unit] = ???

  def renameCollection(newCollectionNamespace: MongoNamespace): Future[Unit] = ???

  def renameCollection(newCollectionNamespace: MongoNamespace, options: RenameCollectionOptions): Future[Unit] = ???

}
