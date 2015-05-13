package com.evojam.mongodb.client

import com.mongodb.{ ReadPreference, WriteConcern }
import org.bson.codecs.configuration.CodecRegistry

class MongoDatabaseImpl(name: String, codec: CodecRegistry, readPref: ReadPreference, writeConcern: WriteConcern,
  executor: ObservableOperationExecutor) extends MongoDatabase {

}
