package com.evojam.mongodb.client.integration.mongocollection

import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import org.bson.Document
import org.specs2.mutable.Specification

import com.evojam.mongodb.client._
import com.evojam.mongodb.client.util.DocumentGenerator

class MapReduceSpec extends Specification with DocumentGenerator {
  sequential

  val collection =
    MongoClients.create().database("testdb")
      .collection("mapreduce")

  val docs = documents(Some(1000))
  val propName = docs.headOption
    .map(doc => asScalaSet(doc.keySet())
      .headOption.getOrElse(throw new Exception("Document must have at least one property.")))
    .getOrElse(throw new Exception("There must be at least one document generated."))

  "MongoCollection mapReduce" should {

    Await.ready(collection.drop(), Duration.Inf)
    Await.ready(collection.insertAll(docs), Duration.Inf)

    "map and reduce" in {
      val res = collection.mapReduce[Document](
        s"""
          |function() {
          |  emit(this["${propName}"], 1);
          |}
        """.stripMargin,
        s"""
          |function(key, values) {
          |  return Array.sum(values);
          |}
        """.stripMargin)
        .collect[Document]

      res should not be empty.await

      val expected =
        docs
          .map(doc => (doc.get(propName), 1))
          .foldLeft(Map[AnyRef, Int]())((map, tpl) => {
            map.get(tpl._1)
              .map(curr => map + (tpl._1 -> (tpl._2 + curr)))
              .getOrElse(map + (tpl._1 -> tpl._2))
          })

      res.map(_.length) must be_==(expected.size).await
      res.map(_.forall(doc =>
        expected(doc.get("_id")) == doc.getDouble("value")
      )) must beTrue.await
    }
  }
}
