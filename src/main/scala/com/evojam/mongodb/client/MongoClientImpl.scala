package com.evojam.mongodb.client

import scala.concurrent.ExecutionContext

import com.mongodb.connection.Cluster
import org.bson.BsonDocument
import com.evojam.mongodb.client.cursor.ListDatabasesCursor

private[client] class MongoClientImpl(
  cluster: Cluster,
  override val settings: MongoClientSettings,
  executor: ObservableOperationExecutor) extends MongoClient {

  lazy val defaultDatabase =
    new MongoDatabase(settings.defaultDatabaseName, settings.readPreference, settings.writeConcern, executor)

  override def database() = defaultDatabase

  override def database(name: String) =
    new MongoDatabase(name, settings.readPreference, settings.writeConcern, executor)

  override def databaseNames()(implicit exc: ExecutionContext) =
    ListDatabasesCursor(settings.readPreference, executor)
      .observable[BsonDocument]()
      .map(_.getString("name").getValue)
      .toList.toBlocking.toFuture

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
