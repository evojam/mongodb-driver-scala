package com.evojam.mongodb.client

import com.mongodb.connection.Cluster
import org.bson.codecs.configuration.CodecRegistry
import rx.lang.scala.Observable

private[client] class MongoClientImpl(cluster: Cluster, settings: MongoClientSettings,
  executor: ObservableOperationExecutor, codecs: CodecRegistry) extends MongoClient {

  override def getDatabase(name: String) =
    new MongoDatabaseImpl(name, codecs, settings.readPreference, settings.writeConcern, executor)

  override def getSettings = settings

  override def listDatabases(): Observable[String] = ???

  override def listDatabaseNames() = listDatabases().toList.toBlocking.toFuture

  override def close() = cluster.close()
}


object MongoClientImpl {

}