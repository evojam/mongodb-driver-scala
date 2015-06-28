package com.evojam.mongodb.client.integration

import org.bson.Document
import org.specs2.mutable.Specification
import com.evojam.mongodb.client._
import com.evojam.mongodb.client.util.IndexSpecSetup

class IndexSpec extends Specification with IndexSpecSetup {
  "MongoDB Indexes" should {
    sequential

    "create single-key index" in {
      collection.createIndex(new Document("testIndex1", 1)).flatMap(_ =>
        collection.listIndexes().collect[Document]) must haveSize[List[Document]](2).await(10)
    }

    "create multi-key index" in {
      val index = new Document("testIndex1", 1).append("testIndex2", 1).append("testIndex3", 1)

      collection.createIndex(index).flatMap(_ =>
        collection.listIndexes().collect[Document]) must haveSize[List[Document]](2).await(10)
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
