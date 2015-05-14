package com.evojam.mongodb.client

import scala.concurrent.Future

import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.{ ReadPreference, WriteConcern }
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

import com.evojam.mongodb.client.iterable.MongoIterable

trait MongoDatabase {

  def withCodecRegistry(codecRegistry: CodecRegistry): MongoDatabase
  def withReadPreference(readPreference: ReadPreference): MongoDatabase
  def withWriteConcern(writeConcern: WriteConcern): MongoDatabase

  def getCollection(collectionName: String): MongoCollection[Document]
  def getCollection[T](collectionName: String, documentClass: Class[T]): MongoCollection[T]

  def runCommand(command: Bson): Future[Document]
  def runCommand(command: Bson, readPref: ReadPreference): Future[Document]
  def runCommand[T](command: Bson, resultClass: Class[T]): Future[T]
  def runCommand[T](command: Bson, resultClass: Class[T], readPref: ReadPreference): Future[T]

  def drop(): Future[Unit]

  def listCollectionNames(): MongoIterable[String]
  def listCollections[T](): MongoIterable[T]

  def createCollection(collectionName: String): Future[Unit]
  def createCollection(collectionName: String, options: CreateCollectionOptions): Future[Unit]
}
