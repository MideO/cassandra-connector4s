package com.github.mideo.cassandra.testing.support

import java.nio.file.{Files, Path, Paths}
import java.util.UUID

import com.datastax.driver.core.ResultSet
import com.datastax.driver.mapping.Mapper
import com.github.mideo.cassandra.connector.{CassandraConnectorTest, TestUser}
import com.github.mideo.cassandra.connector.repository.ConnectedRepository

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class IntegrationTests extends CassandraConnectorTest {

  behavior of "IntegrationTests"


  val bootstrap: Path = Paths.get(migrationsDirectoryLocation + "/bootstrap.cql")
  val users_table: Path = Paths.get(migrationsDirectoryLocation + "/create_user_table.cql")

  if (!Files.exists(migrationsDirectoryLocation)) Files.createDirectory(migrationsDirectoryLocation)
  if (!Files.exists(bootstrap)) Files.createFile(bootstrap)
  if (!Files.exists(users_table)) Files.createFile(users_table)


  Files.write(bootstrap, "CREATE KEYSPACE cassandra_connector WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1 };".getBytes)
  Files.write(users_table, "CREATE TABLE cassandra_connector.users (user_id UUID , name text, PRIMARY KEY(user_id));".getBytes)


  val connectedRepository: ConnectedRepository = ConnectedInMemoryRepository.connect("cassandra_connector", migrationsResourceDirectory)

  "ConnectedInMemoryRepository" should "start EmbeddedDb" in {

    ConnectedInMemoryRepository.EmbeddedCassandra.isRunning should be(true)
    ConnectedInMemoryRepository.EmbeddedCassandra.runningPort should not equal null
    ConnectedInMemoryRepository.EmbeddedCassandra.getHosts should equal(List("localhost"))
  }


  "ConnectedInMemoryRepository" should "run migrations " in {
    val result: ResultSet = Await.result(connectedRepository.connectedSession.session map {
      s => {
        s.execute("USE cqlmigrate;")
        s.execute("Select * from locks;")
      }
    }, 5 seconds)
    result.getColumnDefinitions.getName(0) should equal("name")
    result.getColumnDefinitions.getName(1) should equal("client")
  }


  "materialise repository " should " get data from repository" in {
    val userMapper: Mapper[TestUser] = Await.result(connectedRepository.repositoryMapper.materialise(classOf[TestUser]), 5 seconds)

    val pk = UUID.randomUUID
    val mideo = new TestUser(pk, "mideo")

    userMapper.save(mideo)

    val actual = userMapper.get(pk)

    actual.userId should equal(mideo.userId)
    actual.name should equal(mideo.name)

  }

  "materialise repository " should "delete data from repository" in {
    val userMapper: Mapper[TestUser] = Await.result(connectedRepository.repositoryMapper.materialise(classOf[TestUser]), 5 seconds)

    val pk = UUID.randomUUID
    val mideo = new TestUser(pk, "mideo1")

    userMapper.save(mideo)

    userMapper.delete(pk)

    val actual = userMapper.get(pk)
    actual should be (null)
  }

  "materialise repository " should "update data from repository" in {
    val userMapper: Mapper[TestUser] = Await.result(connectedRepository.repositoryMapper.materialise(classOf[TestUser]), 5 seconds)

    val pk = UUID.randomUUID
    val mideo2 = new TestUser(pk, "mide02")
    val mideo3 = new TestUser(pk, "mideo3")

    userMapper.save(mideo2)

    userMapper.save(mideo3)

    val actual = userMapper.get(pk)
    actual.userId should equal(mideo3.userId)
    actual.name should equal(mideo3.name)
  }
}
