package com.evojam.mongodb.client.integration

import com.mongodb.client.model.IndexOptions

import org.bson.Document
import org.specs2.mutable.Specification

import com.evojam.mongodb.client.codec.Codecs._
import com.evojam.mongodb.client.model.IndexModel
import com.evojam.mongodb.client.util.IndexSpecSetup

class IndexSpec extends Specification with IndexSpecSetup {

  "MongoDB Indexes" should {
    sequential

    "create many single-key indexes" in {
      val indexes = List(
        IndexModel(new Document("testIndex1", 1), new IndexOptions()),
        IndexModel(new Document("testIndex2", 1), new IndexOptions()),
        IndexModel(new Document("testIndex3", 1), new IndexOptions()))

      collection.createIndexes(indexes).flatMap(_ =>
        collection.listIndexes().collect[Document]) must haveSize[List[Document]](4).await(10)
    }

    "create many multi-key indexes" in {
      val indexes = List(
        IndexModel(new Document("testIndex1", 1).append("testIndex2", 1).append("testIndex3", 1), new IndexOptions()),
        IndexModel(new Document("testIndex4", 1).append("testIndex5", 1).append("testIndex6", 1), new IndexOptions()),
        IndexModel(new Document("testIndex7", 1).append("testIndex8", 1).append("testIndex9", 1), new IndexOptions()))

      collection.createIndexes(indexes).flatMap(_ =>
        collection.listIndexes().collect[Document]) must haveSize[List[Document]](4).await(10)
    }

    "drop a multi-key index by key names" in {
      val index = new Document("testIndex1", 1).append("testIndex2", 1).append("testIndex3", 1)

      val indexes = for {
        _ <- collection.createIndex(index)
        _ <- collection.dropIndex(index)
        result <- collection.listIndexes().collect[Document]
      } yield result

      indexes must haveSize[List[Document]](1).await(10)
    }
  }
}
