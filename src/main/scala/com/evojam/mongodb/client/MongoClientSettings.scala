package com.evojam.mongodb.client

import java.util.Arrays._

import com.mongodb.connection._
import com.mongodb.{ MongoCredential, ReadPreference, WriteConcern }
import org.bson.codecs.configuration.CodecRegistries._
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.{ BsonValueCodecProvider, DocumentCodecProvider, ValueCodecProvider }

case class MongoClientSettings(
  readPreference: ReadPreference,
  writeConcern: WriteConcern,
  credentialList: List[MongoCredential],
  codecRegistry: CodecRegistry,
  clusterSettings: ClusterSettings,
  socketSettings: SocketSettings,
  heartbeatSocketSettings: SocketSettings,
  connectionPoolSettings: ConnectionPoolSettings,
  serverSettings: ServerSettings,
  sslSettings: SslSettings) {

  def readPreference(preference: ReadPreference): MongoClientSettings = copy(readPreference = preference)

  def writeConcern(concern: WriteConcern): MongoClientSettings = copy(writeConcern = concern)

  def credentialList(credentials: List[MongoCredential]): MongoClientSettings = copy(credentialList = credentials)

  def codecRegistry(registry: CodecRegistry): MongoClientSettings = copy(codecRegistry = registry)

  def clusterSettings(settings: ClusterSettings): MongoClientSettings = copy(clusterSettings = settings)

  def socketSettings(settings: SocketSettings): MongoClientSettings = copy(socketSettings = settings)

  def heartbeatSocketSettigns(settings: SocketSettings): MongoClientSettings = copy(heartbeatSocketSettings = settings)

  def connectionPoolSettings(settings: ConnectionPoolSettings): MongoClientSettings =
    copy(connectionPoolSettings = settings)

  def serverSettings(settings: ServerSettings): MongoClientSettings = copy(serverSettings = settings)

  def sslSettings(settings: SslSettings): MongoClientSettings = copy(sslSettings = settings)
}

object MongoClientSettings {

  object Default {
    val readPreference: ReadPreference = ReadPreference.primary()

    val writeConcern: WriteConcern = WriteConcern.NORMAL

    val credentialList: List[MongoCredential] = Nil

    val codecRegistry: CodecRegistry =
      fromProviders(asList(new ValueCodecProvider, new DocumentCodecProvider, new BsonValueCodecProvider))

    val clusterSettings: ClusterSettings = ClusterSettings.builder().build()

    val socketSettings: SocketSettings = SocketSettings.builder().build()

    val heartbeatSocketSettings: SocketSettings = SocketSettings.builder().build()

    val connectionPoolSettings: ConnectionPoolSettings = ConnectionPoolSettings.builder().build()

    val serverSettings: ServerSettings = ServerSettings.builder().build()

    val sslSettings: SslSettings = SslSettings.builder().build()
  }

  def apply(): MongoClientSettings = apply(
    Default.readPreference,
    Default.writeConcern,
    Default.credentialList,
    Default.codecRegistry,
    Default.clusterSettings,
    Default.socketSettings,
    Default.heartbeatSocketSettings,
    Default.connectionPoolSettings,
    Default.serverSettings,
    Default.sslSettings)
}