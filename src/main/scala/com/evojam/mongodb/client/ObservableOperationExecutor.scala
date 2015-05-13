package com.evojam.mongodb.client

import com.mongodb.ReadPreference
import com.mongodb.operation.{ AsyncReadOperation, AsyncWriteOperation }
import rx.lang.scala.Observable

trait ObservableOperationExecutor {

  def execute[T](asyncReadOperation: AsyncReadOperation[T], readPreference: ReadPreference): Observable[T]

  def execute[T](asyncWriteOperation: AsyncWriteOperation[T]): Observable[T]
}