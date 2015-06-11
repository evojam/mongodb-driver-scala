package com.evojam.mongodb.client.integration

import java.util

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import org.bson._
import org.specs2.mutable.Specification

import com.evojam.mongodb.client.MongoClients
import com.evojam.mongodb.client.codec.Codecs._

class MongoCollectionSpec extends Specification {

  sequential

  "MongoCollection" should {
    val collection =
      MongoClients.create().database("local")
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
  }

  "MongoCollection" should {
    val collection =
      MongoClients.create().database("testdb")
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

  "MongoCollection findAndModify" should {
    val collection =
      MongoClients.create().database("testdba")
        .collection("acollection")

    Await.ready(collection.delete(new Document()), Duration.Inf)
    Await.ready(collection.insert(new Document("_id", "any")), Duration.Inf)

    val document = new Document()
    document.append("_id", "docid")
    document.append("a", "first")
    document.append("b", "second")

    val expectedDocument = new Document()
    expectedDocument.append("_id", "docid")
    expectedDocument.append("a", "first")
    expectedDocument.append("b", "secondreplaced")

    val selector = new Document("_id", "docid")
    val update1 = new Document("$set", new Document("b", "secondreplaced"))
    val update2 = new Document("$set", new Document("b", "secondanother"))

    "fail to find and create nothing" in {
      collection
        .findAndModify(
          selector,
          new Document("$set", document),
          upsert = false)
        .collect[Document] must beNone.await

    }

    "insert document into collection" in {

      collection
        .findAndModify(
          selector,
          new Document("$set", document),
          upsert = true)
        .collect[Document] must beSome(document).await

    }

    "modify and return updated" in {

      collection
        .findAndModify(selector, update1)
        .collect[Document] must beSome(expectedDocument).await

    }

    "modify and return former" in {
      collection
        .findAndModify(selector, update2, returnFormer = true)
        .collect[Document] must beSome(expectedDocument).await
    }

  }
}
