package com.evojam.mongodb.client.integration.mongocollection

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import org.bson.Document
import org.specs2.mutable.Specification

import com.evojam.mongodb.client._

class FindAndUpdateSpec extends Specification {
  sequential

  "MongoCollection findAndUpdate" should {
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
        .findAndUpdate(
          selector,
          new Document("$set", document))
        .upsert(false)
        .collect[Document] must beNone.await
    }

    "insert document into collection" in {
      collection
        .findAndUpdate(
          selector,
          new Document("$set", document))
        .upsert(true)
        .collect[Document] must beSome(document).await
    }

    "update and return updated" in {
      collection
        .findAndUpdate(selector, update1)
        .collect[Document] must beSome(expectedDocument).await
    }

    "update and return former" in {
      collection
        .findAndUpdate(selector, update2)
        .returnFormer(true)
        .collect[Document] must beSome(expectedDocument).await
    }
  }
}
