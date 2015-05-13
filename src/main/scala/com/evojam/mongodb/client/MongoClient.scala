package com.evojam.mongodb.client

import java.io.Closeable

import scala.concurrent.Future

import rx.lang.scala.Observable

trait MongoClient extends Closeable {

  def getDatabase(name: String): MongoDatabase

  def getSettings: MongoClientSettings

  def listDatabaseNames(): Future[List[String]]

  def listDatabases(): Observable[String]
}
