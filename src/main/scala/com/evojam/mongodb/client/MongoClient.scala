package com.evojam.mongodb.client

import java.io.Closeable

import scala.concurrent.{ExecutionContext, Future}

trait MongoClient extends Closeable {

  def database(): MongoDatabase

  def database(name: String): MongoDatabase

  def settings: MongoClientSettings

  def databaseNames()(implicit exc: ExecutionContext): Future[List[String]]
}
