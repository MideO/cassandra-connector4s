## cassandra-connector
[![Build Status](https://travis-ci.org/MideO/cassandra-connector4s.svg?branch=master)](https://travis-ci.org/MideO/cassandra-connector4s)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.mideo/cassandra-connector4s_2.12/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.mideo%22%20a%3A%22cassandra-connector4s_2.12%22)
## Cassandra DB bundle to enable 
* Connection to cassandra
* Connection to cassandra embedded cassadra for testing
* Generic materialised repository 
* Managing migrations (with [Cqlmigrate](https://github.com/sky-uk/cqlmigrate#what-it-does) and [CqlMigration.scala](src/main/scala/com/github/mideo/cassandra/connector/repository/CqlMigration.scalaCqlMigration.scala))

##### Docs?
  See Functional tests: [IntegrationTests.scala](src/test/scala/com/github/mideo/cassandra/testing/support/IntegrationTests.scala)
  
#### Uage

##### Create cassandra-connector.conf in resources directory
```bash
cassandra-connector {
  cluster {
    username: user
    password: pass
    keyspace: cassandra_connector
    port: 9402
    contactPoints: localhost
  }
  session {
    consistencyLevel: local_quorum
  }
}
```
##### Define a repository
```scala

@Table(keyspace = "cassandra_connector", name = "users", caseSensitiveKeyspace = false, caseSensitiveTable = false)
class User() {
  @PartitionKey
  @Column(name = "user_id")
  @BeanProperty var userId: UUID = _
  @BeanProperty var name: String = _

  // Define auxiliary constructor due to cassandra driver limitation i.e. using reflection 
  def this(userId: UUID, name: String) = {
    this()
    this.userId = userId
    this.name = name
  }
}
```

##### Perform CRUD Ops
```scala
// Create a connected repository
val connectedRepository = ConnectedRepository.connect("cassandra_connector")

// Run migrations if required
connectedRepository.runMigrations("aGivenDirectoryWithDotCqlFiles")

// Materialise a repository
val userMapper: Future[Mapper[TestUser]] = connectedRepository.repositoryMapper.materialise(classOf[TestUser])

// Create an instance of repository entity
val mideo = new User(UUID.randomUUID, "mideo")

// Create an instance of repository entity
userMapper.map { _.save(mideo) }

// Get user
userMapper.map { _.get(mideo.userId) }


// Delete user
userMapper.map { _.delete(mideo.userId) }
```

##### Testing with [EmbeddedCassandra](https://github.com/jsevellec/cassandra-unit)

```scala
// Connect with the ConnectedInMemoryRepository object
val connectedRepository: ConnectedRepository = ConnectedInMemoryRepository.connect("cassandra_connector")

// embedded cassandra is started and off you go!

```
