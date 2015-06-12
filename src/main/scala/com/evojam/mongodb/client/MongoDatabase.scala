package com.evojam.mongodb.client

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.operation.CommandReadOperation
import com.mongodb.operation.CommandWriteOperation
import org.bson.BsonDocument
import org.bson.codecs.Codec
import org.bson.codecs.Encoder

import com.evojam.mongodb.client.codec.Codecs._
import com.evojam.mongodb.client.iterable.ListCollectionsIterable
import com.evojam.mongodb.client.iterable.MongoIterable
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
    ListCollectionsIterable[BsonDocument](
      name,
      ReadPreference.primary(),
      executor)
      .collect[BsonDocument]()
      .map(_.map(_.getString("name").getValue))

  def listCollections[T: Encoder](): MongoIterable =
    ListCollectionsIterable[T](name, ReadPreference.primary(), executor)

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

  def drop(): Future[Unit] = ???

  def createCollection(collectionName: String): Future[Unit] = ???

  def createCollection(collectionName: String, options: CreateCollectionOptions): Future[Unit] = ???

}
