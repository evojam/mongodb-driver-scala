package com.evojam.mongodb.client.util

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import org.specs2.specification.BeforeExample

import com.evojam.mongodb.client.MongoClients

trait IndexSpecSetup extends BeforeExample {

  val db = MongoClients.create().database("foodb")
  val collection = db.collection("bar")

  def before = {
    Await.ready(collection.dropIndexes(), 10.seconds)
  }

}
