package com.evojam.mongodb.client.builder

import java.util.concurrent.TimeUnit

import scala.concurrent.{Future, ExecutionContext}

import com.mongodb.MongoNamespace
import com.mongodb.operation.FindAndDeleteOperation
import org.bson.codecs.Codec

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.codec.Reader
import com.evojam.mongodb.client.util.BsonUtil

case class FindAndRemoveBuilder[T: Codec](
  namespace: MongoNamespace,
  executor: ObservableOperationExecutor,
  filter: T,
  sort: Option[T] = None,
  projection: Option[T] = None,
  maxTimeMS: Option[Long] = None) extends SingleResultBuilder {

  require(namespace != null, "namespace cannot be null")
  require(executor != null, "executor cannot be null")
  require(filter != null, "filter cannot be null")
  require(sort != null, "sort cannot be null")
  require(projection != null, "projection cannot be null")

  override def collect[R]()(implicit reader: Reader[R], exc: ExecutionContext): Future[Option[R]] =
    executor.executeAsync(findAndDeleteOperation(
      new FindAndDeleteOperation(namespace, reader.codec)))
      .map(Option(_).map(reader.read))
      .toBlocking.toFuture

  def filter(filter: T): FindAndRemoveBuilder[T] = {
    require(filter != null, "filter cannot be null")
    this.copy(filter = filter)
  }

  def sort(sort: T): FindAndRemoveBuilder[T] = {
    require(sort != null, "sort cannot be null")
    this.copy(sort = Some(sort))
  }

  def projection(projection: T): FindAndRemoveBuilder[T] = {
    require(projection != null, "projection cannot be null")
    this.copy(projection = Some(projection))
  }

  def maxTime(maxTime: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) = {
    require(maxTime >= 0L, "maxTime cannot be negative")
    require(timeUnit != null, "timeUnit cannot be null")
    this.copy(maxTimeMS = Some(TimeUnit.MILLISECONDS.convert(maxTime, timeUnit)))
  }

  private def findAndDeleteOperation[R](op: FindAndDeleteOperation[R]) = {
    op.filter(BsonUtil.toBson(filter))
    sort.foreach(sortDoc => op.sort(BsonUtil.toBson(sortDoc)))
    projection.foreach(projectionDoc => op.projection(BsonUtil.toBson(projectionDoc)))
    maxTimeMS.foreach(op.maxTime(_, TimeUnit.MILLISECONDS))
    op
  }
}
