package com.evojam.mongodb.client

import scala.collection.JavaConversions._

import com.mongodb.ConnectionString
import com.mongodb.connection._
import com.mongodb.connection.netty.NettyStreamFactory
import com.mongodb.management.JMXConnectionPoolListener

object MongoClients {

  def buildSettings(connectionString: ConnectionString) = MongoClientSettings()
    .clusterSettings(ClusterSettings.builder.applyConnectionString(connectionString).build)
    .connectionPoolSettings(ConnectionPoolSettings.builder.applyConnectionString(connectionString).build)
    .credentialList(connectionString.getCredentialList.toList)
    .sslSettings(SslSettings.builder.applyConnectionString(connectionString).build)
    .socketSettings(SocketSettings.builder.applyConnectionString(connectionString).build)

  def create: MongoClient = create(new ConnectionString("mongodb://localhost"))

  def create(settings: MongoClientSettings): MongoClient =
    MongoClientImpl(settings, createCluster(settings, getStreamFactory(settings)))

  def create(connectionString: String): MongoClient = create(new ConnectionString(connectionString))

  def create(connectionString: ConnectionString): MongoClient = create(buildSettings(connectionString))

  private def createCluster(settings: MongoClientSettings, streamFactory: StreamFactory): Cluster =
    new DefaultClusterFactory().create(settings.clusterSettings, settings.serverSettings, settings
      .connectionPoolSettings, streamFactory, getHeartbeatStreamFactory(settings), settings.credentialList, null, new
        JMXConnectionPoolListener, null)

  private def getHeartbeatStreamFactory(settings: MongoClientSettings): StreamFactory =
    getStreamFactory(settings.heartbeatSocketSettings, settings.sslSettings)

  private def getStreamFactory(settings: MongoClientSettings): StreamFactory =
    getStreamFactory(settings.socketSettings, settings.sslSettings)

  private def getStreamFactory(socketSettings: SocketSettings, sslSettings: SslSettings): StreamFactory =
    System.getProperty("org.mongodb.async.type", "netty") match {
      case "netty" =>
        new NettyStreamFactory(socketSettings, sslSettings)
      case streamType @ "nio2" =>
        if(sslSettings.isEnabled) {
          throw new IllegalArgumentException("Unsupported stream type " + streamType + " when SSL is enabled. Please " +
                                             "use Netty " + "instead")
        }
        new AsynchronousSocketChannelStreamFactory(socketSettings, sslSettings)
      case unsuportedStreamType =>
        throw new IllegalArgumentException("Unsupported stream type " + unsuportedStreamType)
    }
}
