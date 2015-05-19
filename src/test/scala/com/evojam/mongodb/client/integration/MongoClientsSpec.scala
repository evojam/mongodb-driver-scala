package com.evojam.mongodb.client.integration

import com.evojam.mongodb.client.{ MongoClient, MongoClients }
import org.specs2.mutable.Specification

class MongoClientsSpec extends Specification {

  "MongoClients" should {
    "create MongoClient and connect" in {
      MongoClients.create must beAnInstanceOf[MongoClient]
    }

    "list databases" in {
      MongoClients.create.listDatabaseNames() must not be empty.await(10)
    }

    "list collections" in {
      val db = MongoClients.create.getDatabase("local")

      db.listCollectionNames must not be empty.await(10)
    }

  }
}
