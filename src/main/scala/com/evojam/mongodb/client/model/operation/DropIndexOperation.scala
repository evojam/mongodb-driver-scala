package com.evojam.mongodb.client.model.operation

import scala.language.implicitConversions

import com.mongodb.MongoNamespace
import com.mongodb.operation.{DropIndexOperation => MongoDropIndexOperation}
import org.bson.{BsonDocument, BsonInt32, BsonString}

case class DropIndexOperation(
  namespace: MongoNamespace,
  indexName: String) {

  require(namespace != null, "namespace cannot be null")
  require(indexName != null, "indexName cannot be null")
}

object DropIndexOperation {

  private def generateIndexName(index: BsonDocument): String = {
    import scala.collection.JavaConversions._

    val indexName = index.keySet.foldLeft(new StringBuilder) { (indexName, key) =>

      if (indexName.nonEmpty) indexName.append('_')

      indexName.append(key).append('_')

      index.get(key) match {
        case value: BsonInt32 => indexName.append(value.getValue)
        case value: BsonString => indexName.append(value.getValue.replace(' ', '_'))
      }
    }

    indexName.toString()
  }

  def apply(namespace: MongoNamespace, keys: BsonDocument): DropIndexOperation =
    DropIndexOperation(namespace, generateIndexName(keys))

  implicit def dropIndexOperation2Mongo(dio: DropIndexOperation): MongoDropIndexOperation =
    new MongoDropIndexOperation(dio.namespace, dio.indexName)
}
