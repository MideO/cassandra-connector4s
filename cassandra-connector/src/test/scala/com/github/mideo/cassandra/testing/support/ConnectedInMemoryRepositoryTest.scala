package com.github.mideo.cassandra.testing.support

import java.nio.file.{Files, Path, Paths}

import com.datastax.driver.core.ResultSet
import com.github.mideo.cassandra.connector.CassandraConnectorTest
import com.github.mideo.cassandra.connector.repository.ConnectedRepository

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ConnectedInMemoryRepositoryTest extends CassandraConnectorTest {

  behavior of "ConnectedInMemoryRepositoryTest"


  val bootstrap: Path = Paths.get(migrationsDirectoryLocation + "/bootstrap.cql")
  val users_table: Path = Paths.get(migrationsDirectoryLocation + "/create_user_table.cql")

  if (!Files.exists(migrationsDirectoryLocation)) Files.createDirectory(migrationsDirectoryLocation)
  if (!Files.exists(bootstrap)) Files.createFile(bootstrap)
  if (!Files.exists(users_table)) Files.createFile(users_table)


  Files.write(bootstrap, "CREATE KEYSPACE cassandra_connector WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1 };".getBytes)
  Files.write(users_table, "CREATE TABLE cassandra_connector.users (user_id UUID PRIMARY KEY, name text);".getBytes)


  val connectedRepository: ConnectedRepository = ConnectedInMemoryRepository.connect("cassandra_connector", migrationsResourceDirectory)

  it should "start EmbeddedDb" in {

    ConnectedInMemoryRepository.EmbeddedCassandra.isRunning should be(true)
    ConnectedInMemoryRepository.EmbeddedCassandra.runningPort should not equal null
    ConnectedInMemoryRepository.EmbeddedCassandra.getHosts should equal(List("localhost"))
  }


  it should "run migrations " in {
    val result: ResultSet = Await.result(connectedRepository.connectedSession.session map {
      s => {
        s.execute("USE cqlmigrate;")
        s.execute("Select * from locks;")
      }
    }, 5 seconds)
    result.getColumnDefinitions.getName(0) should equal("name")
    result.getColumnDefinitions.getName(1) should equal("client")
  }
}
