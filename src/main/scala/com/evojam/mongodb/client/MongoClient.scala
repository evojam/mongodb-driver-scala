package com.evojam.mongodb.client

import java.io.Closeable

import scala.concurrent.Future

trait MongoClient extends Closeable {

  def database(): MongoDatabase

  def database(name: String): MongoDatabase

  def settings: MongoClientSettings

  def databaseNames(): Future[List[String]]
}
