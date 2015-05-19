package com.evojam.mongodb.client

import com.evojam.mongodb.client.iterable.{ ListDatabasesIterableImpl, MongoIterable }
import com.mongodb.connection.Cluster
import org.bson.BsonDocument

private[client] class MongoClientImpl(
  cluster: Cluster,
  override val settings: MongoClientSettings,
  executor: ObservableOperationExecutor) extends MongoClient {

  override def getDatabase(name: String) =
    new MongoDatabase(name, settings.codecRegistry, settings.readPreference, settings.writeConcern, executor)

  override def listDatabases(): MongoIterable[String] =
    ListDatabasesIterableImpl[BsonDocument](
      classOf[BsonDocument],
      settings.codecRegistry,
      settings.readPreference,
      executor)
      .map(_.getString("name").getValue)

  override def listDatabaseNames() = listDatabases().collect()

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
