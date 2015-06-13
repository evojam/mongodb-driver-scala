package com.evojam.mongodb.client.integration

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import org.bson._
import org.specs2.mutable.Specification

import com.evojam.mongodb.client.MongoClients
import com.evojam.mongodb.client.codec.Codecs._

class MongoCollectionSpec extends Specification {
  sequential

  "MongoCollection" should {
    val collection = MongoClients.create()
      .database("foo")
      .collection("bar")

    Await.ready(collection.drop(), Duration.Inf)

    val docs = List(
      new Document("prop", "value1"),
      new Document("prop", "value2"),
      new Document("prop", "value3"),
      new Document("prop", "value4"),
      new Document("prop", "value5"))

    "count on empty collection" in {
      collection.count must be_==(0).await
    }

    "find nothing in empty collection" in {
      collection.find.collect[Document] must haveSize[List[Document]](0).await
    }

    "insert single element to the collection" in {
      val res = collection
        .insert(new Document("prop", "value"))
        .flatMap(_ => collection.find().collect[Document])

      res must not be empty.await
    }

    "count on non-empty collection" in {
      collection.count must be_==(1L).await
    }

    "drop the collection" in {
      val res = collection
        .drop()
        .flatMap(_ => collection.count)

      res must be_==(0L).await
    }

    "insert multiple documents to the collection" in {
      val res = collection
        .insertAll(docs)
        .flatMap(_ => collection.count())

      res must be_==(docs.size).await
    }

    "find all documents in the collection" in {
      collection
        .find()
        .collect[Document] must haveSize[List[Document]](docs.size).await
    }

    "find particular document from the collection" in {
      val res = collection
        .find(new Document("prop", "value2"))
        .head[Document]
        .map(_.getString("prop"))

      res must be_==("value2").await
    }

    "result to a single element list" in {
      val res = collection
        .find(new Document("prop", "value2"))
        .collect[Document]

      res must haveSize[List[Document]](1).await
    }

    "limit result to single element" in {
      val res = collection
        .find()
        .limit(1)
        .collect[Document]

      res must haveSize[List[Document]](1).await

      val resValue = res
        .map(_.headOption)
        .map(_.map(_.getString("prop")))

      resValue must be_==(docs.headOption.map(_.getString("prop"))).await
    }

    "skip few elements" in {
      val res = collection
        .find()
        .skip(4)
        .collect[Document]

      res must haveSize[List[Document]](1).await

      val resValue = res
        .map(_.lastOption)
        .map(_.map(_.getString("prop")))

      resValue must be_==(docs.lastOption.map(_.getString("prop"))).await
    }

    "skip few and limit to single element" in {
      val res = collection
        .find()
        .skip(4)
        .limit(1)
        .collect[Document]

      res must haveSize[List[Document]](1).await

      val resValue = res
        .map(_.lastOption)
        .map(_.map(_.getString("prop")))

      resValue must be_==(docs.lastOption.map(_.getString("prop"))).await
    }

    "skip few and limit to more than one element" in {
      val res = collection
        .find()
        .skip(2)
        .limit(2)
        .collect[Document]

      res must haveSize[List[Document]](2).await

      val resValues = res
        .map(_.map(_.getString("prop")))

      resValues must be_==(docs.drop(2).take(2).map(_.getString("prop"))).await
    }
  }
}
