package com.evojam.mongodb.client.iterable

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.TimeUnit

import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import org.bson.codecs.Codec
import org.bson.codecs.Encoder

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.DistinctOperation

private[client] case class DistinctIterable[T: Encoder](
  fieldName: String,
  filter: Option[T],
  namespace: MongoNamespace,
  readPreference: ReadPreference,
  executor: ObservableOperationExecutor,
  private val maxTimeMS: Option[Long] = None) extends MongoIterable {

  require(fieldName != null, "fieldName cannot be null")
  require(filter != null, "filter cannot be null")
  require(namespace != null, "namespace cannot be null")
  require(readPreference != null, "readPreference cannot be null")
  require(executor != null, "executor cannot be null")

  override protected def rawHead[R: Codec]() =
    execute[R].head

  override protected def rawHeadOpt[R: Codec]() =
    execute[R].headOpt

  override protected def rawForeach[R: Codec](f: R => Unit) =
    execute.foreach(f)

  override protected def rawCursor[R: Codec]() =
    execute.cursor()

  override protected def rawCursor[R: Codec](batchSize: Int) =
    execute.cursor(batchSize)

  override protected def rawCollect[R: Codec]() =
    execute[R].collect

  def filter(filter: T) = this.copy(filter = Option(filter))

  def maxTime(maxTime: Long, timeUnit: TimeUnit) = {
    require(timeUnit != null, "timeUnit cannot be null")
    this.copy(maxTimeMS = Some(TimeUnit.MILLISECONDS.convert(maxTime, timeUnit)))
  }

  private def execute[R: Codec]: OperationIterable[R] =
    execute(distinctOperation[R])

  private def execute[R](operation: DistinctOperation[T, R])(implicit c: Codec[R]): OperationIterable[R] =
    OperationIterable(operation, readPreference, executor)

  private def distinctOperation[R]()(implicit c: Codec[R]) =
    DistinctOperation[T, R](namespace, fieldName, c, filter, maxTimeMS)
}
