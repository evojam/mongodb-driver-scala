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

  def getCollection(collectionName: String): MongoCollection[Document] = ???

  def getCollection[T](collectionName: String, documentClass: Class[T]): MongoCollection[T] = ???

  def runCommand(command: Bson): Future[Document] = ???

  def runCommand(command: Bson, readPref: ReadPreference): Future[Document] = ???

  def runCommand[T](command: Bson, resultClass: Class[T]): Future[T] = ???

  def runCommand[T](command: Bson, resultClass: Class[T], readPref: ReadPreference): Future[T] = ???

  def drop(): Future[Unit] = ???

  def listCollections[T](): MongoIterable[T] = ???

  def createCollection(collectionName: String): Future[Unit] = ???

  def createCollection(collectionName: String, options: CreateCollectionOptions): Future[Unit] = ???

}
