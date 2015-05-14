package com.evojam.mongodb.client

import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.{ ReadPreference, WriteConcern }
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

class MongoDatabaseImpl(name: String, codec: CodecRegistry, readPref: ReadPreference, writeConcern: WriteConcern,
  executor: ObservableOperationExecutor) extends MongoDatabase {

  override def withCodecRegistry(codecRegistry: CodecRegistry) = ???

  override def getCollection(collectionName: String) = ???

  override def getCollection[T](collectionName: String, documentClass: Class[T]) = ???

  override def listCollections[T]() = ???

  override def createCollection(collectionName: String) = ???

  override def createCollection(collectionName: String, options: CreateCollectionOptions) = ???

  override def runCommand(command: Bson) = ???

  override def runCommand(command: Bson, readPref: ReadPreference) = ???

  override def runCommand[T](command: Bson, resultClass: Class[T]) = ???

  override def runCommand[T](command: Bson, resultClass: Class[T], readPref: ReadPreference) = ???

  override def withReadPreference(readPreference: ReadPreference) = ???

  override def drop() = ???

  override def listCollectionNames() = ???

  override def withWriteConcern(writeConcern: WriteConcern) = ???
}
