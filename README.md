# MongoDB Driver Scala

![Travis Build Status](https://travis-ci.org/evojam/mongodb-driver-scala.svg)

[![Codacy Badge](https://www.codacy.com/project/badge/305004faf8194a27b93e3a9d2b04bbb9)](https://www.codacy.com/app/arturbankowski_3078/mongodb-driver-scala)

Built on top of the new [Java Core Async Driver](http://mongodb.github.io/mongo-java-driver/) from MongoDB,
the **MongoDB Driver Scala** allows you to achieve end-to-end fully asynchronous non-blocking I/O.

## Why?
When MongoDB released the new *Core Async Driver*, we decided to start this project for a few reasons
* The driver we were extensively using, *ReactiveMongo*, has been [less and less active](https://github.com/ReactiveMongo/ReactiveMongo/commits/master).
* We wanted an API that would allow us to write idiomatic Scala.

## Going Async

```scala
val client = MongoClients.create() // Default connection to localhost:27017
val testCollection = client.collection("test")

// Fetch all documents from a collection
val docs: Future[List[Document]] =
  testCollection
    .find(new Document())
    .collect[Document]
```

## Installation

**MongoDB Driver Scala** is available from Sonatype, simply add it as a dependency to your `build.sbt`

```
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
libraryDependencies += "com.evojam" % "mongo-driver-scala_2.11" % "0.1.0-SNAPSHOT"
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