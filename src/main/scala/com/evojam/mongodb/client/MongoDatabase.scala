package com.evojam.mongodb.client

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import com.mongodb.client.model.CreateCollectionOptions
import org.bson.BsonDocument
import org.bson.Document
import org.bson.codecs.Encoder
import org.bson.conversions.Bson

import com.evojam.mongodb.client.codec.Codecs.bsonDocumentCodec
import com.evojam.mongodb.client.iterable.ListCollectionsIterable
import com.evojam.mongodb.client.iterable.MongoIterable

class MongoDatabase(
  name: String,
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

  def runCommand(command: Bson): Future[Document] = ???

  def runCommand(command: Bson, readPref: ReadPreference): Future[Document] = ???

  def runCommand[T](command: Bson, resultClass: Class[T]): Future[T] = ???

  def runCommand[T](command: Bson, resultClass: Class[T], readPref: ReadPreference): Future[T] = ???

  def drop(): Future[Unit] = ???

  def createCollection(collectionName: String): Future[Unit] = ???

  def createCollection(collectionName: String, options: CreateCollectionOptions): Future[Unit] = ???

}
