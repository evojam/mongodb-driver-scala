package com.evojam.mongodb.client.util

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

import org.bson.Document
import org.specs2.mutable.BeforeAfter

import com.evojam.mongodb.client.MongoClients
import com.evojam.mongodb.client.codec.Codecs._

trait SampleNonEmptyCollection extends BeforeAfter {
  val db = MongoClients.create().database("foodb")
  val collection = db.collection("bar")

  def collectionContent: List[Document]

  override def before = {
    Await.ready(collection.insertAll(collectionContent), 10 seconds)
  }

  override def after = {
    Await.ready(collection.drop(), 10 seconds)
    ()
  }
}
