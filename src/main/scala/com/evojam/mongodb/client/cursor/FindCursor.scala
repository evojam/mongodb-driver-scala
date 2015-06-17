package com.evojam.mongodb.client.cursor

import java.util.concurrent.TimeUnit

import com.mongodb.CursorType
import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import org.bson.codecs.Codec
import org.bson.codecs.Encoder

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.operation.FindOperation
import com.evojam.mongodb.client.model.options.FindOptions

private[client] case class FindCursor[T: Encoder](
  filter: Option[T],
  findOptions: FindOptions[T],
  namespace: MongoNamespace,
  readPreference: ReadPreference,
  executor: ObservableOperationExecutor) extends Cursor {

  require(filter != null, "filter cannot be null")
  require(findOptions != null, "findOptions cannot be null")
  require(namespace != null, "namespace cannot be null")
  require(readPreference != null, "readPreference cannot be null")
  require(executor != null, "executor cannt be null")

  override protected def rawHead[R: Codec]() =
    execute[R](queryOperation[R].copy(batchSize = 0, limit = -1)).head

  override protected def rawHeadOpt[R: Codec]() =
    execute[R](queryOperation[R].copy(batchSize = 0, limit = -1)).headOpt

  override protected def rawForeach[R: Codec](f: R => Unit) =
    execute.foreach(f)

  override protected def rawObservable[R: Codec]() =
    execute.observable()

  override protected def rawObservable[R: Codec](batchSize: Int) =
    execute(queryOperation[R]().copy(batchSize = batchSize)).observable(batchSize)

  override protected def rawCollect[R: Codec]() =
    execute[R].collect()

  private def execute[R: Codec]: OperationCursor[R] =
    execute(queryOperation[R])

  private def execute[R: Codec](fo: FindOperation[T, R]): OperationCursor[R] =
    OperationCursor(fo, readPreference, executor)

  private def queryOperation[R]()(implicit c: Codec[R]) =
    FindOperation[T, R](
      namespace = namespace,
      decoder = c,
      filter = filter,
      batchSize = findOptions.batchSize,
      skip = findOptions.skip,
      limit = findOptions.limit,
      maxTime = findOptions.maxTime,
      maxTimeUnit = findOptions.maxTimeUnit,
      modifiers = findOptions.modifiers,
      projection = findOptions.projection,
      sort = findOptions.sort,
      cursorType = findOptions.cursorType,
      noCursorTimeout = findOptions.noCursorTimeout,
      oplogRelay = findOptions.oplogRelay,
      partial = findOptions.partial,
      slaveOk = readPreference.isSlaveOk)

  def filter(filter: T) =
    FindCursor[T](Option(filter), findOptions, namespace, readPreference, executor)

  def limit(limit: Int) =
    FindCursor[T](filter, findOptions.copy(limit = limit), namespace, readPreference, executor)

  def skip(skip: Int) =
    FindCursor[T](filter, findOptions.copy(skip = skip), namespace, readPreference, executor)

  def maxTime(maxTime: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) =
    FindCursor[T](filter, findOptions.copy(maxTime = maxTime, maxTimeUnit = timeUnit),
      namespace, readPreference, executor)

  def modifiers(modifiers: T) =
    FindCursor[T](filter, findOptions.copy(modifiers = Option(modifiers)),
      namespace, readPreference, executor)

  def projection(projection: T) =
    FindCursor[T](filter, findOptions.copy(projection = Option(projection)),
      namespace, readPreference, executor)

  def sort(sort: T) =
    FindCursor[T](filter, findOptions.copy(sort = Option(sort)),
      namespace, readPreference, executor)

  def noCursorTimeout(noCursorTimeout: Boolean) =
    FindCursor[T](filter, findOptions.copy(noCursorTimeout = noCursorTimeout),
      namespace, readPreference, executor)

  def oplogRelay(oplogRelay: Boolean) =
    FindCursor[T](filter, findOptions.copy(oplogRelay = oplogRelay),
      namespace, readPreference, executor)

  def partial(partial: Boolean) =
    FindCursor[T](filter, findOptions.copy(partial = partial),
      namespace, readPreference, executor)

  def cursorType(cursorType: CursorType) =
    FindCursor[T](filter, findOptions.copy(cursorType = cursorType),
      namespace, readPreference, executor)

  def batchSize(batchSize: Int) =
    FindCursor[T](filter, findOptions.copy(batchSize = batchSize),
      namespace, readPreference, executor)
}
