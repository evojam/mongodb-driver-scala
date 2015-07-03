package com.evojam.mongodb.client

import scala.concurrent.ExecutionContext

import com.mongodb.ReadPreference
import com.mongodb.async.SingleResultCallback
import com.mongodb.binding.AsyncClusterBinding
import com.mongodb.connection.Cluster
import com.mongodb.operation.{AsyncReadOperation, AsyncWriteOperation}
import rx.lang.scala.{Observable, Subscriber}
import rx.lang.scala.schedulers.ExecutionContextScheduler

private[client] class ObservableOperationExecutorImpl(cluster: Cluster) extends ObservableOperationExecutor {

  override def executeAsync[T](op: AsyncReadOperation[T], rp: ReadPreference)(implicit exc: ExecutionContext) = {

    val binding = ObservableOperationExecutorImpl.asyncReadWriteBinding(rp, cluster)

    val observable = Observable[T](sub => op.executeAsync(binding, bindWithCallback(sub)))

    observable.subscribeOn(scheduler)

    observable.doOnTerminate(binding.release())

    observable.first
  }

  override def executeAsync[T](op: AsyncWriteOperation[T])(implicit exc: ExecutionContext) = {

    val binding = ObservableOperationExecutorImpl.asyncReadWriteBinding(ReadPreference.primary, cluster)

    val observable = Observable[T](sub => op.executeAsync(binding, bindWithCallback(sub)))

    observable.subscribeOn(scheduler)

    observable.doOnTerminate(binding.release())

    observable.first
  }

  private def bindWithCallback[T](subscriber: Subscriber[T]) =
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

  private def scheduler(implicit exc: ExecutionContext) =
    ExecutionContextScheduler(exc)
}

object ObservableOperationExecutorImpl {
  private def asyncReadWriteBinding(readPreference: ReadPreference, cluster: Cluster) = {
    require(readPreference != null, "readPreference cannot be null")
    new AsyncClusterBinding(cluster, readPreference)
  }

  def apply(cluster: Cluster) = new ObservableOperationExecutorImpl(cluster)
}
