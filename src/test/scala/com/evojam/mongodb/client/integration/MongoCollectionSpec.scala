package com.evojam.mongodb.client.integration

import org.bson.Document
import org.specs2.mutable.Specification

import com.evojam.mongodb.client.MongoClients

class MongoCollectionSpec extends Specification {

  // TODO: Seed data into collection prior to testing

  "MongoCollection" should {
    val collection =
      MongoClients.create.getDatabase("local")
        .collection("startup_log")

    "count on collections" in {
      collection.count must beGreaterThan(0L).await(10)
    }

    "find on collections" in {
      val docs = collection
        .find[Document]()
        .collect

      docs must not be empty.await(10)
    }

    "limit find to single result" in {
      val docs = collection
        .find[Document]()
        .limit(1)

      docs.collect must haveSize[List[Document]](1).await(10)
    }

    "list indexes" in {
      collection.listIndexes.collect must not be empty.await(10)
    }
  }

  "MongoCollection" should {
    val collection =
      MongoClients.create.getDatabase("testdb")
        .collection("acollection")

    "insert and then delete document from collection" in {
      val insertRes = collection.insertOne(new Document())
        .flatMap(_ => collection.find[Document]().collect)

      insertRes must not be empty.await(10)

      val deleteRes = collection.deleteAll
        .flatMap(_ => collection.find[Document]().collect)

      deleteRes must haveSize[List[Document]](0).await(10)
    }
  }
}
