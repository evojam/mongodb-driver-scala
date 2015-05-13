package com.evojam.mongodb.client

import com.mongodb.connection._
import com.mongodb.{ MongoCredential, ReadPreference, WriteConcern }
import org.bson.codecs.configuration.CodecRegistry

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

}
