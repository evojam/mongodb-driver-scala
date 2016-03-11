# This project is no longer maintained.

We originally developed a wrapper for [MongoDB Java Driver] that would provide
idiomatic Scala API. We did that because there was no good Scala solution at the
time. There was no official Scala driver and the community-based [ReactiveMongo]
lacked support and stayed behind the Java versions. The Java driver, on the
other hand, was quite slow in certain use cases. In fact, once ReactiveMongo
provided support for MongoDB 3.0 it turned out that it was over tenfold faster
than the original Java Driver. In general we had to choose between suboptimal
Java driver or outdated ReactiveMongo.

This has changed. [Our recent microbenchmarks] show that the current versions of
Java driver are no longer inferior in terms of speed. There is even an [official
Scala driver], which is really just a small facade built on top of the async
Java driver.

[Reactivemongo][ReactiveMongo], on the other hand, become a go-to library for
Akka and Play fans. It uses Play Iteratees and was designed with good scaling in
mind. There is also a [ReactiveMongo Play module] for easy integration with
Playframework and its JSON library. So please switch either to [ReactiveMongo]
or the [official Scala driver].

  [MongoDB Java Driver]: https://docs.mongodb.org/ecosystem/drivers/java/
  [ReactiveMongo]: http://reactivemongo.org/
  [official Scala driver]: http://mongodb.github.io/mongo-scala-driver/
  [ReactiveMongo Play module]: https://github.com/ReactiveMongo/Play-ReactiveMongo
  [Our recent microbenchmarks]: https://github.com/evojam/mongo-drivers-benchmarks

# Original Readme follows:

![Travis Build Status](https://travis-ci.org/evojam/mongodb-driver-scala.svg)
[![Codacy Badge](https://www.codacy.com/project/badge/305004faf8194a27b93e3a9d2b04bbb9)](https://www.codacy.com/app/evojam/mongodb-driver-scala)
[![Gitter chat](https://badges.gitter.im/evojam/mongodb-driver-scala.png)](https://gitter.im/evojam/mongodb-driver-scala "Gitter chat")

Built on top of the new [Java Core Async Driver](http://mongodb.github.io/mongo-java-driver/) from MongoDB,
the **MongoDB Driver Scala** allows you to achieve end-to-end fully asynchronous non-blocking I/O.

The library is designed for MongoDB 3.0.x and will be updated to support newer releases. However it successfuly works
with older revisions of Mongo server except for some specific commands that may fail.

## Why?
When MongoDB released the new *Core Async Driver*, we decided to start this project for a few reasons
* The driver we were extensively using, *ReactiveMongo*, has been [less and less active](https://github.com/ReactiveMongo/ReactiveMongo/commits/master).
* We wanted an API that would allow us to write idiomatic Scala.

## Going Async

```scala
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.evojam.mongodb.client._
import org.bson.Document

val client = MongoClients.create() // Default connection to localhost:27017
val testCollection = client.collection("test")

// Fetch all documents from a collection
val docs: Future[List[Document]] =
  testCollection
    .find()
    .collect[Document]
```

## Requirements

- JDK7 or above

## Installation

**MongoDB Driver Scala** is available from Sonatype, simply add it as a dependency to your `build.sbt`

Current stable version:

```scala
resolvers += Resolver.sonatypeRepo("releases")
libraryDependencies += "com.evojam" % "mongo-driver-scala_2.11" % "0.5.0"
```

Current snapshot:

```scala
resolvers += Resolver.sonatypeRepo("snapshots")
libraryDependencies += "com.evojam" % "mongo-driver-scala_2.11" % "0.5.0-SNAPSHOT"
```

## Framework Integration

To make use of this fast non-blocking IO driver we're currently developing a [module](https://github.com/evojam/play-mongodb-driver) for Play Framework 2.4,
which will provide an idiomatic, easy-to-use Scala API.

## Contributing

If you're interested in high performance, reactive programming (Rx Observables, Futures), and NoSQL databases then you've found a great project
to contribute to and we welcome all help - pull requests, reporting bugs, suggesting new features, and so on.

## License
```
This software is licensed under the Apache 2 license, quoted below.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
```
