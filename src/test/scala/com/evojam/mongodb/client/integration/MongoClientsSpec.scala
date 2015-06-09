package com.evojam.mongodb.client.integration

import com.evojam.mongodb.client.{ MongoDatabase, MongoClient, MongoClients }
import org.specs2.mutable.Specification
import org.bson.Document

class MongoClientsSpec extends Specification {

  "MongoClients" should {
    "create MongoClient and connect" in {
      MongoClients.create must beAnInstanceOf[MongoClient]
    }

    "list databases" in {
      MongoClients.create.databaseNames() must not be empty.await(10)
    }

    "list collections" in {
      val db = MongoClients.create.database("local")

      db.listCollectionNames() must not be empty.await(10)
    }

    "return default databsae" in {
      MongoClients.create.database().name must be equalTo "test"
    }
  }
}
