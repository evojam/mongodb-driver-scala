package com.evojam.mongodb.client.cursor

import java.util.concurrent.TimeUnit

import scala.concurrent.{ExecutionContext, Future}

import com.mongodb.operation.MapReduceWithInlineResultsOperation
import com.mongodb.{MongoNamespace, ReadPreference}
import org.bson.BsonJavaScript
import org.bson.codecs.{Codec, Encoder}
import rx.lang.scala.Observable

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.options.MapReduceAction.MapReduceAction
import com.evojam.mongodb.client.model.options.MapReduceOptions
import com.evojam.mongodb.client.util.BsonUtil

private[client] case class MapReduceCursor[T: Encoder](
  mapFunction: String,
  reduceFunction: String,
  readPreference: ReadPreference,
  namespace: MongoNamespace,
  executor: ObservableOperationExecutor,
  options: MapReduceOptions[T]) extends Cursor {

  require(mapFunction != null, "mapFunction cannot be null")
  require(reduceFunction != null, "reduceFunction cannot be null")
  require(readPreference != null, "readPreference cannot be null")
  require(namespace != null, "namespace cannot be null")
  require(executor != null, "executor cannot be null")

  def scope(scope: T) =
    this.copy(options = options.copy(scope = Option(scope)))

  def sort(sort: T) =
    this.copy(options = options.copy(sort = Option(sort)))

  def filter(filter: T) =
    this.copy(options = options.copy(filter = Option(filter)))

  def limit(limit: Int) =
    this.copy(options = options.copy(limit = Some(limit)))

  def finalizeFunction(finalizeFunction: String) =
    this.copy(options = options.copy(finalizeFunction = Some(finalizeFunction)))

  def jsMode(jsMode: Boolean) =
    this.copy(options = options.copy(jsMode = Some(jsMode)))

  def verbose(verbose: Boolean) =
    this.copy(options = options.copy(verbose = verbose))

  def maxTime(maxTime: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) =
    this.copy(options = options.copy(maxTimeMS = Option(TimeUnit.MILLISECONDS.convert(maxTime, timeUnit))))

  def action(action: MapReduceAction) =
    this.copy(options = options.copy(action = action))

  def sharded(sharded: Boolean) =
    this.copy(options = options.copy(sharded = Some(sharded)))

  def nonAtomic(nonAtomic: Boolean) =
    this.copy(options = options.copy(nonAtomic = Some(nonAtomic)))

  def batchSize(batchSize: Int) =
    this.copy(options = options.copy(batchSize = Some(batchSize)))

  override protected def rawHead[R: Codec]()(implicit exc: ExecutionContext) =
    cursor().head()

  override protected def rawObservable[R: Codec]()(implicit exc: ExecutionContext) =
    cursor().observable()

  override protected def rawObservable[R: Codec](batchSize: Int)(implicit exc: ExecutionContext) =
    cursor().observable(batchSize)

  override protected def rawForeach[R: Codec](f: (R) => Unit)(implicit exc: ExecutionContext) =
    cursor().foreach(f)

  private def cursor[R: Codec](): OperationCursor[R] =
    cursor(mapReduceOperation[R])

  private def cursor[R: Codec](op: MapReduceWithInlineResultsOperation[R]): OperationCursor[R] =
    OperationCursor[R](op, readPreference, executor)

  private def mapReduceOperation[R]()(implicit codec: Codec[R]) = {
    val op = new MapReduceWithInlineResultsOperation[R](
      namespace,
      new BsonJavaScript(mapFunction),
      new BsonJavaScript(reduceFunction),
      codec)
    options.filter.foreach(filter => op.filter(BsonUtil.toBson(filter)))
    options.limit.foreach(op.limit)
    options.maxTimeMS.foreach(op.maxTime(_, TimeUnit.MILLISECONDS))
    options.jsMode.foreach(op.jsMode)
    options.scope.foreach(scope => op.scope(BsonUtil.toBson(scope)))
    options.sort.foreach(sort => op.sort(BsonUtil.toBson(sort)))
    op.verbose(options.verbose)
    op
  }
}