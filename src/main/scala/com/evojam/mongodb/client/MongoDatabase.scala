package com.evojam.mongodb.client

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import com.mongodb.operation._
import org.bson.BsonDocument
import org.bson.Document
import org.bson.codecs.Codec
import org.bson.codecs.Encoder
import com.evojam.mongodb.client.cursor._
import com.evojam.mongodb.client.model.options.CreateCollectionOptions
import com.evojam.mongodb.client.util.BsonUtil

class MongoDatabase(
  val name: String,
  readPref: ReadPreference,
  writeConcern: WriteConcern,
  executor: ObservableOperationExecutor) {

  require(name != null, "name cannot be null")
  require(readPref != null, "readPref cannot be null")
  require(writeConcern != null, "writeConcern cannot be null")
  require(executor != null, "executor cannot be null")

  def withReadPreference(readPreference: ReadPreference): MongoDatabase =
    new MongoDatabase(name, readPreference, writeConcern, executor)

  def withWriteConcern(writeConcern: WriteConcern): MongoDatabase =
    new MongoDatabase(name, readPref, writeConcern, executor)

  def listCollectionNames(): Future[List[String]] =
    ListCollectionsCursor[BsonDocument](
      name,
      ReadPreference.primary(),
      executor)
      .collect[BsonDocument]()
      .map(_.map(_.getString("name").getValue))

  def listCollections[T: Encoder](): Cursor =
    ListCollectionsCursor[T](name, ReadPreference.primary(), executor)

  def collection(collectionName: String): MongoCollection =
    MongoCollectionImpl(new MongoNamespace(name, collectionName), readPref, writeConcern, executor)

  def runCommand[T: Codec](command: T): Future[T] = {
    require(command != null, "command cannot be null")

    executor.executeAsync(
      new CommandWriteOperation(name, BsonUtil.toBson(command), implicitly[Codec[T]]))
      .toBlocking.toFuture
  }

  def runCommand[T: Codec](command: T, readPref: ReadPreference): Future[T] = {
    require(command != null, "command cannot be null")
    require(readPref != null, "readPref cannot be null")

    executor.executeAsync(
      new CommandReadOperation(name, BsonUtil.toBson(command), implicitly[Codec[T]]),
      readPref)
      .toBlocking.toFuture
  }

  def drop(): Future[Unit] =
    executor.executeAsync(new DropDatabaseOperation(name))
      .toBlocking.toFuture.map(_ => ())

  def createCollection(collectionName: String): Future[Unit] =
    createCollection(collectionName, CreateCollectionOptions())

  def createCollection(
    collectionName: String,
    options: CreateCollectionOptions): Future[Unit] =
    createCollection[Document](collectionName, options, None)

  def createCollection[T: Codec](
    collectionName: String,
    options: CreateCollectionOptions,
    storageEngineOptions: Option[T]): Future[Unit] = {
    val opts = new CreateCollectionOperation(name, collectionName)
      .capped(options.capped)
      .sizeInBytes(options.size)
      .autoIndex(options.autoIndex)
      .maxDocuments(options.maxDocuments)
      .usePowerOf2Sizes(options.usePowerOf2Sizes)
    storageEngineOptions.map(storageOptions =>
      opts.storageEngineOptions(BsonUtil.toBson(storageOptions)))
    executor.executeAsync(opts).toBlocking.toFuture.map(_ => ())
  }
}
