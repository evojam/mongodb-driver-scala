package com.evojam.mongodb.client

import scala.concurrent.Future

import com.evojam.mongodb.client.codec.Reader

trait SingleResult {
  def collect[R: Reader](): Future[Option[R]]
}
