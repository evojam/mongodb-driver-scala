package com.evojam.mongodb.client.model.operation

import scala.language.implicitConversions

import com.mongodb.operation.{ CreateIndexesOperation => MongoCreateIndexesOperation }
import com.mongodb.MongoNamespace
import com.mongodb.bulk.IndexRequest

case class CreateIndexesOperation(
  namespace: MongoNamespace,
  requests: java.util.List[IndexRequest]) {

  require(namespace != null, "namespace cannot be null")
  require(requests != null, "requests cannot be null")
}

object CreateIndexesOperation {
  implicit def createIndexOperation2Mongo(cio: CreateIndexesOperation): MongoCreateIndexesOperation =
    new MongoCreateIndexesOperation(cio.namespace, cio.requests)
}
