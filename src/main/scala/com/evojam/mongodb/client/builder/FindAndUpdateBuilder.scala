package com.evojam.mongodb.client.builder

import java.util.concurrent.TimeUnit

import scala.concurrent.{ExecutionContext, Future}

import com.mongodb.MongoNamespace
import com.mongodb.operation.FindAndUpdateOperation
import org.bson.codecs.Codec

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.codec.Reader
import com.evojam.mongodb.client.util.BsonUtil

case class FindAndUpdateBuilder[T: Codec](
  namespace: MongoNamespace,
  executor: ObservableOperationExecutor,
  update: T,
  filter: Option[T] = None,
  sort: Option[T] = None,
  projection: Option[T] = None,
  returnFormer: Boolean = false,
  upsert: Boolean = false,
  maxTimeMS: Option[Long] = None) extends SingleResultBuilder {

  require(namespace != null, "namespace cannot be null")
  require(executor != null, "executor cannot be null")
  require(filter != null, "filter cannot be null")
  require(sort != null, "sort cannot be null")
  require(update != null, "update cannot be null")
  require(projection != null, "projection cannot be null")

  override def collect[R]()(implicit reader: Reader[R], exc: ExecutionContext): Future[Option[R]] =
    executor.executeAsync(findAndUpdateOperation(
      new FindAndUpdateOperation(namespace, reader.codec, BsonUtil.toBson(update))))
      .map(Option(_).map(reader.read))
      .toBlocking.toFuture

  def filter(filter: T): FindAndUpdateBuilder[T] = {
    require(filter != null, "filter cannot be null")
    this.copy(filter = Some(filter))
  }

  def sort(sort: T): FindAndUpdateBuilder[T] = {
    require(sort != null, "sort cannot be null")
    this.copy(sort = Some(sort))
  }

  def projection(projection: T): FindAndUpdateBuilder[T] = {
    require(projection != null, "projection cannot be null")
    this.copy(projection = Some(projection))
  }

  def returnFormer(returnFormer: Boolean): FindAndUpdateBuilder[T] =
    this.copy(returnFormer = returnFormer)

  def upsert(upsert: Boolean): FindAndUpdateBuilder[T] =
    this.copy(upsert = upsert)

  def maxTime(maxTime: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) = {
    require(maxTime >= 0L, "maxTime cannot be negative")
    require(timeUnit != null, "timeUnit cannot be null")
    this.copy(maxTimeMS = Some(TimeUnit.MILLISECONDS.convert(maxTime, timeUnit)))
  }

  private def findAndUpdateOperation[R](op: FindAndUpdateOperation[R]) = {
    filter.foreach(filterDoc => op.filter(BsonUtil.toBson(filterDoc)))
    sort.foreach(sortDoc => op.sort(BsonUtil.toBson(sortDoc)))
    projection.foreach(projectionDoc => op.projection(BsonUtil.toBson(projectionDoc)))
    op.returnOriginal(returnFormer)
    op.upsert(upsert)
    maxTimeMS.foreach(op.maxTime(_, TimeUnit.MILLISECONDS))
    op
  }
}
