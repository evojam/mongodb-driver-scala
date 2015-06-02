package com.evojam.mongodb.client.iterable

import java.util.concurrent.TimeUnit

import com.mongodb.CursorType
import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import org.bson.codecs.Codec
import org.bson.codecs.Encoder

import com.evojam.mongodb.client.ObservableOperationExecutor
import com.evojam.mongodb.client.model.operation.FindOperation
import com.evojam.mongodb.client.model.options.FindOptions

case class FindIterable[T: Encoder](
  filter: Option[T],
  findOptions: FindOptions[T],
  namespace: MongoNamespace,
  readPreference: ReadPreference,
  executor: ObservableOperationExecutor) extends MongoIterable {

  require(filter != null, "filter cannot be null")
  require(findOptions != null, "findOptions cannot be null")
  require(namespace != null, "namespace cannot be null")
  require(readPreference != null, "readPreference cannot be null")
  require(executor != null, "executor cannt be null")

  override def head[R: Codec] =
    execute[R](queryOperation[R].copy(batchSize = 0, limit = -1)).head

  override def headOpt[R: Codec] =
    execute[R](queryOperation[R].copy(batchSize = 0, limit = -1)).headOpt

  override def foreach[R: Codec](f: R => Unit) =
    execute.foreach(f)

  override def cursor[R: Codec](batchSize: Option[Int]) =
    execute.cursor(batchSize)

  override def collect[R: Codec]() = execute[R].collect()

  private def execute[R: Codec]: OperationIterable[R] = execute(queryOperation[R])

  private def execute[R: Codec](fo: FindOperation[T, R]): OperationIterable[R] =
    OperationIterable(fo, readPreference, executor)

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
    FindIterable[T](Option(filter), findOptions, namespace, readPreference, executor)

  def limit(limit: Int) =
    FindIterable[T](filter, findOptions.copy(limit = limit), namespace, readPreference, executor)

  def skip(skip: Int) =
    FindIterable[T](filter, findOptions.copy(skip = skip), namespace, readPreference, executor)

  def maxTime(maxTime: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) =
    FindIterable[T](filter, findOptions.copy(maxTime = maxTime, maxTimeUnit = timeUnit),
      namespace, readPreference, executor)

  def modifiers(modifiers: T) =
    FindIterable[T](filter, findOptions.copy(modifiers = Option(modifiers)),
      namespace, readPreference, executor)

  def projection(projection: T) =
    FindIterable[T](filter, findOptions.copy(projection = Option(projection)),
      namespace, readPreference, executor)

  def sort(sort: T) =
    FindIterable[T](filter, findOptions.copy(sort = Option(sort)),
      namespace, readPreference, executor)

  def noCursorTimeout(noCursorTimeout: Boolean) =
    FindIterable[T](filter, findOptions.copy(noCursorTimeout = noCursorTimeout),
      namespace, readPreference, executor)

  def oplogRelay(oplogRelay: Boolean) =
    FindIterable[T](filter, findOptions.copy(oplogRelay = oplogRelay),
      namespace, readPreference, executor)

  def partial(partial: Boolean) =
    FindIterable[T](filter, findOptions.copy(partial = partial),
      namespace, readPreference, executor)

  def cursorType(cursorType: CursorType) =
    FindIterable[T](filter, findOptions.copy(cursorType = cursorType),
      namespace, readPreference, executor)

  def batchSize(batchSize: Int) =
    FindIterable[T](filter, findOptions.copy(batchSize = batchSize),
      namespace, readPreference, executor)
}
