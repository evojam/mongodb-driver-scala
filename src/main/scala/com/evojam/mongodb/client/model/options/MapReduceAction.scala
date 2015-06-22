package com.evojam.mongodb.client.model.options

import scala.language.implicitConversions

import com.mongodb.client.model.{MapReduceAction => MongoMapReduceAction}

object MapReduceAction extends Enumeration {
  type MapReduceAction = Value

  val Replace, Merge, Reduce = Value

  implicit def mapReduceAction2Mongo(action: MapReduceAction): MongoMapReduceAction =
    action match {
      case Merge => MongoMapReduceAction.MERGE
      case Reduce => MongoMapReduceAction.REDUCE
      case Replace => MongoMapReduceAction.REPLACE
    }
}
