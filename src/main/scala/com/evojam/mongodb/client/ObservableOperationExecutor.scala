package com.evojam.mongodb.client

import com.mongodb.ReadPreference
import com.mongodb.operation.{ AsyncReadOperation, AsyncWriteOperation }
import rx.lang.scala.Observable

private[client] trait ObservableOperationExecutor {

  def executeAsync[T](asyncReadOperation: AsyncReadOperation[T], readPreference: ReadPreference): Observable[T]

  def executeAsync[T](asyncWriteOperation: AsyncWriteOperation[T]): Observable[T]
}
