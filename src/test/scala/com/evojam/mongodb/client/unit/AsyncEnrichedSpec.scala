package com.evojam.mongodb.client.unit

import com.evojam.mongodb.client.util.AsyncEnriched

import com.mongodb.async.{ AsyncBatchCursor, SingleResultCallback }

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class AsyncEnrichedSpec extends Specification with Mockito with AsyncEnriched {

  "Observable AsyncBatchCursor" should {

    "not call the cursor's next method with no subscribers" in {
      val asyncBatchCursor = mock[AsyncBatchCursor[String]]

      asyncBatchCursor.asObservable

      there was no(asyncBatchCursor).next(any[SingleResultCallback[java.util.List[String]]])
    }

    "call the cursor's next method when subscribed to" in {
      val asyncBatchCursor = mock[AsyncBatchCursor[String]]

      asyncBatchCursor.asObservable.subscribe()

      there was atLeastOne(asyncBatchCursor).next(any[SingleResultCallback[java.util.List[String]]])
    }
  }

}
