package com.evojam.mongodb.client.integration.mongocollection

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Random

import org.bson.Document
import org.specs2.mutable.Specification

import com.evojam.mongodb.client._
import com.evojam.mongodb.client.util.DocumentGenerator

class FindAndRemoveSpec extends Specification with DocumentGenerator {
  sequential

  "MongoCollection findAndRemove" should {
    val db = MongoClients.create().database("testdba")
    val collection = db.collection("findandremove")

    val timeout = 10

    val docs = documents(Some(10))
    val propName = arbitraryProperty(docs)
    val randomDoc = docs(Random.nextInt(docs.length))

    Await.ready(collection.drop(), Duration.Inf)
    Await.ready(collection.insertAll(docs), Duration.Inf)

    "remove nothing from empty collection" in {
      val res = db.collection("findandremoveempty")
        .findAndRemove(randomDoc)
        .collect[Document]

      res must be_==(None).await
    }

    "find and remove a document" in {
      val res = collection
        .findAndRemove(randomDoc)
        .collect[Document]
        .map(_.map(_.get(propName)))

      res must be_==(Some(randomDoc.get(propName))).await

      val count = collection.count()

      count must be_==(docs.length - 1).await
    }

    "fail to remove same document second time" in {
      val res = collection
        .findAndRemove(randomDoc)
        .collect[Document]

      res must be_==(None).await

      val count = collection.count()

      count must be_==(docs.length - 1).await
    }
  }
}
