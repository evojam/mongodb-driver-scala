package com.evojam.mongodb.client.integration

import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import org.bson._
import org.specs2.mutable.Specification

import com.evojam.mongodb.client.MongoClients
import com.evojam.mongodb.client.codec.Codecs._
import com.evojam.mongodb.client.model.options.CreateCollectionOptions

class MongoDatabaseSpec extends Specification {

  sequential

  "MongoDatabase" should {
    val db = MongoClients.create().database("foo")
    val collectionName = "barCollection"

    val insertCommand =
      new BsonDocument()
        .append("insert", new BsonString("bar"))
        .append("documents", new BsonArray(List[BsonDocument](
          new BsonDocument("_id", new BsonString("first")),
          new BsonDocument("_id", new BsonString("second")))))

    val dropCommand = new BsonDocument("dropDatabase", new BsonInt32(1))

    "run insert command" in {
      val res = db.runCommand(insertCommand)

      res must not be empty.await
      res.map(_.get("ok")) must be_==(new BsonInt32(1)).await
      res.map(_.get("n")) must be_==(new BsonInt32(2)).await
    }

    "drop database with command" in {
      val res = db.runCommand(dropCommand)

      res must not be empty.await
      res.map(_.get("dropped")) must be_==(new BsonString("foo")).await
      res.map(_.get("ok")) must be_==(new BsonDouble(1.0)).await
    }

    "create collection with helper method" in {
      Await.ready(db.collection(collectionName).drop(), Duration.Inf)

      val res = db.createCollection(collectionName)
        .flatMap(_ => db.listCollectionNames())
        .map(_.contains(collectionName))

      res must beTrue.await
    }

    "create capped collection with helper method" in {
      Await.ready(db.collection(collectionName).drop(), Duration.Inf)

      val res = db.createCollection(
        collectionName,
        CreateCollectionOptions(capped = true, size = 1024))
        .flatMap(_ => db.listCollectionNames())
        .map(_.contains(collectionName))

      res must beTrue.await
    }

    "drop database with helper method" in {
      val res = db
        .runCommand(insertCommand)
        .flatMap(_ => db.drop())
        .flatMap(_ => MongoClients.create.databaseNames())
        .map(_.contains("foo"))

      res must be_==(false).await
    }
  }
}
