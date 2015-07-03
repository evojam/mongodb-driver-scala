package com.evojam.mongodb.client

import scala.concurrent.ExecutionContext

import com.mongodb.ReadPreference
import com.mongodb.operation.{AsyncReadOperation, AsyncWriteOperation}
import rx.lang.scala.Observable

private[client] trait ObservableOperationExecutor {
  def executeAsync[T](asyncReadOperation: AsyncReadOperation[T], readPreference: ReadPreference)
    (implicit exc: ExecutionContext): Observable[T]

  def executeAsync[T](asyncWriteOperation: AsyncWriteOperation[T])
    (implicit exc: ExecutionContext): Observable[T]
}
