package com.evojam.mongodb.client

import com.mongodb.ReadPreference
import com.mongodb.async.SingleResultCallback
import com.mongodb.binding.AsyncClusterBinding
import com.mongodb.connection.Cluster
import com.mongodb.operation.{ AsyncReadOperation, AsyncWriteOperation }
import rx.lang.scala.Observer
import rx.lang.scala.subjects.ReplaySubject

class ObservableOperationExecutorImpl(cluster: Cluster) extends ObservableOperationExecutor {

  def bindWithCallback[T](subject: Observer[T]) =
    new SingleResultCallback[T] {
      override def onResult(result: T, error: Throwable) = {
        if(error == null) {
          // FIXME: still hate this part but if it works it's good enough until 1st refactor

          Option(result).foreach(subject.onNext)

          subject.onCompleted()
        } else {
          require(result == null, "result cannot be not null")
          subject.onError(error)
        }
      }
    }

  override def execute[T](op: AsyncReadOperation[T], rp: ReadPreference) = {

    val subject = ReplaySubject[T]()

    val binding = ObservableOperationExecutorImpl.asyncReadWriteBinding(rp, cluster)

    subject.doOnTerminate(binding.release())

    op.executeAsync(binding, bindWithCallback(subject))

    subject.first
  }

  override def execute[T](op: AsyncWriteOperation[T]) = {

    val subject = ReplaySubject[T]()

    val binding = ObservableOperationExecutorImpl.asyncReadWriteBinding(ReadPreference.primary, cluster)

    subject.doOnTerminate(binding.release())

    op.executeAsync(binding, bindWithCallback(subject))

    subject.first
  }
}

object ObservableOperationExecutorImpl {
  private def asyncReadWriteBinding(readPreference: ReadPreference, cluster: Cluster) = {
    require(readPreference != null, "readPreference cannot be null")
    new AsyncClusterBinding(cluster, readPreference)
  }

  def apply(cluster: Cluster) = new ObservableOperationExecutorImpl(cluster)
}