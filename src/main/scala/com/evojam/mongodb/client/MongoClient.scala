package com.evojam.mongodb.client

import java.io.Closeable

import scala.concurrent.Future

import com.evojam.mongodb.client.iterable.MongoIterable

trait MongoClient extends Closeable {
  def getDatabase(name: String): MongoDatabase

  def settings: MongoClientSettings

  def databaseNames(): Future[List[String]]
}
