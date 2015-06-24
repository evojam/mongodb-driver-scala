package com.evojam.mongodb.client.builder

import scala.concurrent.Future

import com.evojam.mongodb.client.codec.Reader

trait SingleResultBuilder {
  def collect[R: Reader](): Future[Option[R]]
}
