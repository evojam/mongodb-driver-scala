package com.evojam.mongodb.client

import com.mongodb.ReadPreference
import com.mongodb.async.SingleResultCallback
import com.mongodb.binding.AsyncClusterBinding
import com.mongodb.connection.Cluster
import com.mongodb.operation.{ AsyncReadOperation, AsyncWriteOperation }
import rx.lang.scala.{ Observable, Subscriber }

class ObservableOperationExecutorImpl(cluster: Cluster) extends ObservableOperationExecutor {

  def bindWithCallback[T](subscriber: Subscriber[T]) =
    new SingleResultCallback[T] {
      override def onResult(result: T, error: Throwable) = {
        if(error == null) {
          subscriber.onNext(result)
          subscriber.onCompleted()
        } else {
          require(result == null, "result cannot be not null")
          subscriber.onError(error)
        }
      }
    }

  override def executeAsync[T](op: AsyncReadOperation[T], rp: ReadPreference) = {

    val binding = ObservableOperationExecutorImpl.asyncReadWriteBinding(rp, cluster)

    val observable = Observable[T](sub => op.executeAsync(binding, bindWithCallback(sub)))

    observable.doOnTerminate(binding.release())

    observable.first
  }

  override def executeAsync[T](op: AsyncWriteOperation[T]) = {

    val binding = ObservableOperationExecutorImpl.asyncReadWriteBinding(ReadPreference.primary, cluster)

    val observable = Observable[T](sub => op.executeAsync(binding, bindWithCallback(sub)))

    observable.doOnTerminate(binding.release())

    observable.first
  }
}

object ObservableOperationExecutorImpl {
  private def asyncReadWriteBinding(readPreference: ReadPreference, cluster: Cluster) = {
    require(readPreference != null, "readPreference cannot be null")
    new AsyncClusterBinding(cluster, readPreference)
  }

  def apply(cluster: Cluster) = new ObservableOperationExecutorImpl(cluster)
}
