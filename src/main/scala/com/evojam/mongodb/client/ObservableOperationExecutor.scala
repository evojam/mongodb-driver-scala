package com.evojam.mongodb.client

import com.mongodb.ReadPreference
import com.mongodb.operation.{ AsyncReadOperation, AsyncWriteOperation }
import rx.lang.scala.Observable

/*

public interface AsyncOperationExecutor {
    /**
     * Execute the read operation with the given read preference.
     *
     * @param operation the read operation.
     * @param readPreference the read preference.
     * @param callback the callback to be called when the operation has been executed
     * @param <T> the operations result type.
     */
    <T> void execute(AsyncReadOperation<T> operation, ReadPreference readPreference, SingleResultCallback<T> callback);

    /**
     * Execute the write operation.
     *
     * @param operation the write operation.
     * @param callback the callback to be called when the operation has been executed
     * @param <T> the operations result type.
     */
    <T> void execute(AsyncWriteOperation<T> operation, SingleResultCallback<T> callback);
}


public interface SingleResultCallback<T> {
    /**
     * Called when the operation completes.
     * @param result the result, which may be null.  Always null if e is not null.
     * @param t      the throwable, or null if the operation completed normally
     */
    void onResult(T result, Throwable t);
}
*/

trait ObservableOperationExecutor {

  def executeAsync[T](asyncReadOperation: AsyncReadOperation[T], readPreference: ReadPreference): Observable[T]

  def executeAsync[T](asyncWriteOperation: AsyncWriteOperation[T]): Observable[T]
}