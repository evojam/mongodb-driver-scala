package com.evojam.mongodb.client.builder

import scala.concurrent.{ExecutionContext, Future}

import com.evojam.mongodb.client.codec.Reader

trait SingleResultBuilder {
  def collect[R: Reader]()(implicit exc: ExecutionContext): Future[Option[R]]
}
