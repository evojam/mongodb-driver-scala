package com.evojam.mongodb.client

import scala.concurrent.ExecutionContext.Implicits.global

import com.mongodb.connection.Cluster
import org.bson.BsonDocument

import com.evojam.mongodb.client.codec.Codecs._
import com.evojam.mongodb.client.iterable.ListDatabasesIterable

private[client] class MongoClientImpl(
  cluster: Cluster,
  override val settings: MongoClientSettings,
  executor: ObservableOperationExecutor) extends MongoClient {

  lazy val defaultDatabase =
    new MongoDatabase(settings.defaultDatabaseName, settings.readPreference, settings.writeConcern, executor)

  override def database() = defaultDatabase

  override def database(name: String) =
    new MongoDatabase(name, settings.readPreference, settings.writeConcern, executor)

  override def databaseNames() =
    ListDatabasesIterable(settings.codecRegistry, settings.readPreference, executor)
      .collect[BsonDocument]().map(_.map(_.getString("name").getValue))

  override def close() = cluster.close()
}

object MongoClientImpl {
  def apply(settings: MongoClientSettings, cluster: Cluster) =
    new MongoClientImpl(cluster, settings, ObservableOperationExecutorImpl(cluster))

  def apply(
    settings: MongoClientSettings,
    cluster: Cluster,
    executor: ObservableOperationExecutor) =
    new MongoClientImpl(cluster, settings, executor)
}
