package com.evojam.mongodb.client.integration

import com.evojam.mongodb.client.{ MongoClient, MongoClients }
import org.specs2.mutable.Specification
import org.bson.Document

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

    "count on collections" in {
      val db = MongoClients.create.getDatabase("local")
      val coll = db.collection("startup_log")

      coll.count must beGreaterThan(0L).await(10)
    }

    "find on collections" in {
      val docs = MongoClients.create.getDatabase("local")
        .collection("startup_log")
        .find[Document]
        .collect

      docs must not be empty.await(10)
    }

  }
}
