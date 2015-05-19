package com.evojam.mongodb.client

import scala.concurrent.Future
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import com.mongodb.client.model.CreateCollectionOptions
import org.bson.BsonDocument
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import com.evojam.mongodb.client.iterable.ListCollectionsIterable
import com.evojam.mongodb.client.iterable.MongoIterable
import com.mongodb.MongoNamespace

class MongoDatabase(
  name: String,
  codec: CodecRegistry,
  readPref: ReadPreference,
  writeConcern: WriteConcern,
  executor: ObservableOperationExecutor) {

  require(name != null, "name cannot be null")
  require(codec != null, "codec cannot be null")
  require(readPref != null, "readPref cannot be null")
  require(writeConcern != null, "writeConcern cannot be null")
  require(executor != null, "executor cannot be null")

  def withCodecRegistry(codecRegistry: CodecRegistry): MongoDatabase =
    new MongoDatabase(name, codecRegistry, readPref, writeConcern, executor)

  def withReadPreference(readPreference: ReadPreference): MongoDatabase =
    new MongoDatabase(name, codec, readPreference, writeConcern, executor)

  def withWriteConcern(writeConcern: WriteConcern): MongoDatabase =
    new MongoDatabase(name, codec, readPref, writeConcern, executor)

  def listCollectionNames(): MongoIterable[String] =
    ListCollectionsIterable[BsonDocument](
      name,
      MongoClientSettings.Default.codecRegistry,
      ReadPreference.primary(),
      executor).map(_.getString("name").getValue)

  def listCollections(): MongoIterable[Document] =
    listCollections[Document]

  def listCollections[T <: Any: Manifest](): MongoIterable[T] =
    ListCollectionsIterable[T](
      name,
      codec,
      ReadPreference.primary(),
      executor)

  def collection(collectionName: String): MongoCollection[Document] =
    getCollection[Document](collectionName)

  def getCollection[T <: Any : Manifest](collectionName: String): MongoCollection[T] =
    MongoCollection[T](
      new MongoNamespace(name, collectionName),
      codec,
      readPref,
      writeConcern,
      executor)

  def runCommand(command: Bson): Future[Document] = ???

  def runCommand(command: Bson, readPref: ReadPreference): Future[Document] = ???

  def runCommand[T](command: Bson, resultClass: Class[T]): Future[T] = ???

  def runCommand[T](command: Bson, resultClass: Class[T], readPref: ReadPreference): Future[T] = ???

  def drop(): Future[Unit] = ???

  def createCollection(collectionName: String): Future[Unit] = ???

  def createCollection(collectionName: String, options: CreateCollectionOptions): Future[Unit] = ???

}
