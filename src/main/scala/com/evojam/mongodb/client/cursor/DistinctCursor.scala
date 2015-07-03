package com.evojam.mongodb.client.cursor

import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.TimeUnit

import com.mongodb.{MongoNamespace, ReadPreference}
import org.bson.codecs.{Codec, Encoder}

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.DistinctOperation

private[client] case class DistinctCursor[T: Encoder](
  fieldName: String,
  filter: Option[T],
  namespace: MongoNamespace,
  readPreference: ReadPreference,
  executor: ObservableOperationExecutor,
  private val maxTimeMS: Option[Long] = None) extends Cursor {

  require(fieldName != null, "fieldName cannot be null")
  require(filter != null, "filter cannot be null")
  require(namespace != null, "namespace cannot be null")
  require(readPreference != null, "readPreference cannot be null")
  require(executor != null, "executor cannot be null")
  require(maxTimeMS != null, "maxTimeMS cannot be null")

  override protected def rawHead[R: Codec]()(implicit exc: ExecutionContext) =
    cursor().head()

  override protected def rawForeach[R: Codec](f: R => Unit)(implicit exc: ExecutionContext) =
    cursor().foreach(f)

  override protected def rawObservable[R: Codec]()(implicit exc: ExecutionContext) =
    cursor().observable()

  override protected def rawObservable[R: Codec](batchSize: Int)(implicit exc: ExecutionContext) =
    cursor().observable(batchSize)

  def filter(filter: T) =
    this.copy(filter = Option(filter))

  def maxTime(maxTime: Long, timeUnit: TimeUnit) = {
    require(timeUnit != null, "timeUnit cannot be null")
    this.copy(maxTimeMS = Some(TimeUnit.MILLISECONDS.convert(maxTime, timeUnit)))
  }

  private def cursor[R: Codec](): OperationCursor[R] =
    cursor(distinctOperation[R])

  private def cursor[R](operation: DistinctOperation[T, R])(implicit c: Codec[R]): OperationCursor[R] =
    OperationCursor(operation, readPreference, executor)

  private def distinctOperation[R]()(implicit c: Codec[R]) =
    DistinctOperation[T, R](namespace, fieldName, c, filter, maxTimeMS)
}
