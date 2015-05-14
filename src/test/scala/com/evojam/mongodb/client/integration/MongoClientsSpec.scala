package com.evojam.mongodb.client.integration

import com.evojam.mongodb.client.{ MongoClient, MongoClients }
import org.specs2.mutable.Specification

class MongoClientsSpec extends Specification {

  "MongoClients" should {
    "create MongoClient and connect" in {
      MongoClients.create must beAnInstanceOf[MongoClient]
    }
  }
}
