package com.evojam.mongodb.client.integration

import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Random

import org.bson._
import org.specs2.mutable.Specification

import com.evojam.mongodb.client.MongoClients
import com.evojam.mongodb.client.codec.Codecs._
import com.evojam.mongodb.client.util.DocumentGenerator

class MongoCollectionSpec extends Specification with DocumentGenerator {
  sequential

  "MongoCollection" should {
    val collection = MongoClients.create()
      .database("foo")
      .collection("bar")

    Await.ready(collection.drop(), Duration.Inf)

    val docs = documents(Some(1000))
    val propName = docs.headOption
      .map(doc => asScalaSet(doc.keySet())
        .headOption.getOrElse(throw new Exception("Document must have at least one property.")))
      .getOrElse(throw new Exception("There must be at least one document generated."))

    def randomDoc() =
      docs(Random.nextInt(docs.size))

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

      res must be_==(docs.size).await(100)
    }

    "find all documents in the collection" in {
      collection
        .find()
        .collect[Document] must haveSize[List[Document]](docs.size).await
    }

    "find particular document from the collection" in {
      val doc = randomDoc()
      val res = collection
        .find(doc)
        .head[Document]
        .map(_.get(propName))

      res must be_==(doc.get(propName)).await
    }

    "result to a single element list" in {
      val res = collection
        .find(randomDoc())
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
        .map(_.map(_.get(propName)))

      resValue must be_==(docs.headOption.map(_.get(propName))).await
    }

    "skip few elements" in {
      val res = collection
        .find()
        .skip(4)
        .collect[Document]

      res must haveSize[List[Document]](docs.size - 4).await

      val resValue = res
        .map(_.lastOption)
        .map(_.map(_.get(propName)))

      resValue must be_==(docs.lastOption.map(_.get(propName))).await
    }

    "skip few and limit to single element" in {
      val res = collection
        .find()
        .skip(4)
        .limit(1)
        .collect[Document]

      res must haveSize[List[Document]](1).await

      val resValue = res
        .map(_.headOption)
        .map(_.map(_.get(propName)))

      resValue must be_==(docs.drop(4).take(1).headOption.map(_.get(propName))).await
    }

    "skip few and limit to more than one element" in {
      val res = collection
        .find()
        .skip(2)
        .limit(2)
        .collect[Document]

      res must haveSize[List[Document]](2).await

      val resValues = res
        .map(_.map(_.get(propName)))

      resValues must be_==(docs.drop(2).take(2).map(_.get(propName))).await
    }

    "return elements one by one from cursor" in {
      val res = collection
        .find()
        .cursor[Document]()

      val check = res.zip(docs).map {
        case (docDb, docCol) =>
          (docDb.get(propName), docCol.get(propName))
      }.toList.toBlocking.toFuture

      res.size.toBlocking.toFuture must be_==(docs.size).await

      check.map(_.forall {
        case (p1, p2) => p1 == p2
      }) must be_==(true).await
    }

    "return elements in batches from cursor" in {
      val chunkSize = 3
      val res = collection
        .find()
        .cursor[Document](chunkSize)
        .toList.toBlocking.toFuture
        .map(_.map(_.map(_.get(propName))))

      val docVals = docs.map(_.get(propName))

      res must haveSize[List[List[Object]]](Math.ceil(docs.size.toDouble / chunkSize).toInt).await
    }

    "return empty batch cursor from empty collection" in {
      val res = MongoClients.create()
        .database("foo")
        .collection("emptyone")
        .find()
        .cursor[Document](3)
        .toList.toBlocking.toFuture

      res must haveSize[List[List[Document]]](0).await
    }
  }
}
