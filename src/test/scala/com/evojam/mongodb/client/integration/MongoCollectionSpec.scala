package com.evojam.mongodb.client.integration

import org.bson.Document
import org.specs2.mutable.Specification

import com.evojam.mongodb.client.MongoClients
import com.evojam.mongodb.client.codec.Codecs._

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
        .find()
        .collect[Document]

      docs must not be empty.await(10)
    }

    "limit find to single result" in {
      val docs = collection
        .find()
        .limit(1)

      docs.collect[Document] must haveSize[List[Document]](1).await(10)
    }

    "create and list indexes" in {
      val collection =
        MongoClients.create.getDatabase("foo")
          .collection("bar")

      collection.createIndex(new Document("testIndex", 1))

      collection.listIndexes.collect[Document] must not be empty.await(10)
    }
  }

  "MongoCollection" should {
    val collection =
      MongoClients.create.getDatabase("testdb")
        .collection("acollection")

    "insert and then delete document from collection" in {
      val insertRes = collection.insert(new Document())
        .flatMap(_ => collection.find().collect[Document])

      insertRes must not be empty.await(10)

      val deleteRes = collection.delete(new Document(), multi = true)
        .flatMap(_ => collection.find().collect[Document])

      deleteRes must haveSize[List[Document]](0).await(10)
    }
  }
}
