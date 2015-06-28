package com.evojam.mongodb.client.integration

import org.bson.Document
import org.specs2.mutable.Specification
import com.evojam.mongodb.client._
import com.evojam.mongodb.client.util.SampleNonEmptyCollection

class AggregationSpec extends Specification {
  trait AggregationNonEmptyCollection extends SampleNonEmptyCollection {
    override def collectionContent = List(
      new Document()
        .append("name", "Name1")
        .append("age", 22)
        .append("gender", "male"),
      new Document()
        .append("name", "Name2")
        .append("age", 18)
        .append("gender", "female"),
      new Document()
        .append("name", "Name2")
        .append("age", 33)
        .append("gender", "female"))
  }

  val groupByPipeline = List(
    new Document()
      .append("$group", new Document()
        .append("_id", "$gender")))

  val groupByResult = List(
    new Document()
      .append("_id", "female"),
    new Document()
      .append("_id", "male"))

  "Aggregation" should {
    sequential

    "execute a pipeline" in new AggregationNonEmptyCollection {
      val result = collection.aggregate(groupByPipeline).collect[Document]

      result must not be empty.await(10)
    }

    "group by items" in new AggregationNonEmptyCollection {
      val result = collection.aggregate(groupByPipeline).collect[Document]

      result.map(_.toString) must beEqualTo(groupByResult.toString).await(10)
    }
  }
}
