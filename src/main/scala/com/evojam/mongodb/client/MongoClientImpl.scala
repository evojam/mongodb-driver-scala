package com.evojam.mongodb.client

import com.mongodb.connection.Cluster
import org.bson.codecs.configuration.CodecRegistry
import rx.lang.scala.Observable

private[client] class MongoClientImpl(
  val cluster: Cluster,
  val settings: MongoClientSettings,
  executor: ObservableOperationExecutor) extends MongoClient {

  override def getDatabase(name: String) =
    new MongoDatabaseImpl(name, settings.codecRegistry, settings.readPreference, settings.writeConcern, executor)

  override def getSettings = settings

  override def listDatabases(): Observable[String] = ???

  override def listDatabaseNames() = listDatabases().toList.toBlocking.toFuture

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