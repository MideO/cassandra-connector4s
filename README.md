## cassandra-connector
[![Build Status](https://travis-ci.org/MideO/cassandra-connector4s.svg?branch=master)](https://travis-ci.org/MideO/cassandra-connector4s)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.mideo/cassandra-connector4s_2.12/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.mideo%22%20a%3A%22cassandra-connector4s_2.12%22)
## Cassandra DB bundle to enable 
* Connection to cassandra
* Connection to cassandra embedded cassadra for testing
* Generic materialised repository 
* Managing migrations with [Cqlmigrate](https://github.com/sky-uk/cqlmigrate#what-it-does)

##### Docs?
  See Integration tests: [IntegrationTests.scala](src/test/scala/com/github/mideo/cassandra/testing/support/IntegrationTests.scala)
  
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
    dc: DC1
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

##### Define a customer Repository Accessor
```scala
@Accessor trait TestUserAccessor {
  @Query("SELECT * FROM users") def getAll: Result[TestUser]
  @Query("TRUNCATE users") def truncate: Result[TestUser]
}

```

##### Perform CRUD Ops
```scala
// Either Create a connected Keyspace from the above config without cql migration
val connectedKeyspace = ConnectedKeyspace("cassandra_connector")

// or with cql migration if needed
val connectedKeyspace = ConnectedKeyspace("cassandra_connector", "aGivenDirectoryWithDotCqlFiles")


//Or alternatively, initialise without config file
import com.github.mideo.cassandra.connector.fluent.Connector

val connectedKeyspace = Connector.keyspace("keyspace" )
      .withUserName("mideo")
      .withPassword("password")
      .withConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
      .withContactPoints(List("localhost"))
      .onPort(9402)
      .withDC("DC1")
      .withMigrationsDirectory("aGivenDirectoryWithDotCqlFiles")
      .connect()


// create a connectedTable
val futureTable: Future[ConnectedTable[TestUser, TestUserAccessor]] = connectedKeyspace.materialise[TestUser, TestUserAccessor]


 futureTable map {
      table => table.accessor.truncate
                table.mapper.save(new User(UUID.randomUUID, "mideo"))
                table.accessor.getAll
      } 

// alternatively create a repository
val userMapper: Future[Mapper[TestUser]] = connectedKeyspace.materialise[TestUser]

// Create an instance of repository entity
val mideo = new User(UUID.randomUUID, "mideo")

// Create an instance of repository entity
userMapper.map { _.save(mideo) }

// Get user
userMapper.map { _.get(mideo.userId) }


// Delete user
userMapper.map { _.delete(mideo.userId) }


// alternatively  user custom accessor
val accessor: Future[TestUserAccessor] = connectedKeyspace.materialiseAccessor[TestUserAccessor]

accessor.map { _.getAll }

accessor.map { _.truncate }


```

##### Testing with [EmbeddedCassandra](https://github.com/jsevellec/cassandra-unit)

```scala
// Connect with the ConnectedInMemoryRepository object
val connectedKeyspace: ConnectedKeyspace = ConnectedInMemoryKeyspace("cassandra_connector")

val isRunning = EmbeddedCassandra.isRunning 

val port = EmbeddedCassandra.runningPort 

val hosts = EmbeddedCassandra.getHosts 


```
