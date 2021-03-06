package com.github.mideo.cassandra

import java.nio.file.{Files, Path, Paths}
import java.util.UUID

import com.datastax.driver.core.ResultSet
import com.datastax.driver.mapping.{Mapper, Result}
import com.github.mideo.cassandra.connector.repository.{ConnectedKeyspace, ConnectedTable}
import com.github.mideo.cassandra.connector.{CassandraConnectorTest, TestUser, TestUserAccessor}
import com.github.mideo.cassandra.testing.support.{ConnectedInMemoryKeyspace, EmbeddedCassandra}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class IntegrationTests extends CassandraConnectorTest {

  behavior of "IntegrationTests"

  val bootstrap: Path = Paths.get(migrationsDirectoryLocation + "/bootstrap.cql")
  val users_table: Path = Paths.get(migrationsDirectoryLocation + "/create_user_table.cql")

  if (!Files.exists(migrationsDirectoryLocation)) Files.createDirectory(migrationsDirectoryLocation)
  if (!Files.exists(bootstrap)) Files.createFile(bootstrap)
  if (!Files.exists(users_table)) Files.createFile(users_table)

  Files.write(bootstrap, "CREATE KEYSPACE cassandra_connector WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1 };".getBytes)
  Files.write(users_table, "CREATE TABLE cassandra_connector.users (user_id UUID , name text, PRIMARY KEY(user_id));".getBytes)

  private val connectedKeyspace: ConnectedKeyspace = Await.result(ConnectedInMemoryKeyspace("cassandra_connector", Some(migrationsResourceDirectory)),
    5 minutes)


  override def afterAll(): Unit = {
    connectedKeyspace.close
  }


  "ConnectedInMemoryRepository" should "start EmbeddedDb" in {
    EmbeddedCassandra.isRunning should be(true)
    EmbeddedCassandra.runningPort should not equal null
    EmbeddedCassandra.getHosts should equal(List("localhost"))
  }


  "ConnectedInMemoryRepository" should "run migrations " in {
    val result: ResultSet = Await.result(connectedKeyspace.Session map {
      s => {
        s.execute("USE cqlmigrate;")
        s.execute("Select * from locks;")
      }
    }, 5 seconds)
    result.getColumnDefinitions.getName(0) should equal("name")
    result.getColumnDefinitions.getName(1) should equal("client")
  }


  "materialise repository" should "get data from repository" in {
    val userMapper: Future[Mapper[TestUser]] = connectedKeyspace.materialise[TestUser]

    val pk = UUID.randomUUID
    val mideo = new TestUser(pk, "mideo")

    implicit def intToInteger(x: Int) = java.lang.Integer.valueOf(x)

    Await.result(userMapper.map {
      _.save(mideo)
    }, 10 seconds)


    val actual = Await.result(userMapper.map {
      _.get(pk)
    }, 10 seconds)

    mideo.userId should equal(actual.userId)
    mideo.name should equal(actual.name)

  }


  it should " create accessor (getAll)" in {
    val userMapper: Future[Mapper[TestUser]] = connectedKeyspace.materialise[TestUser]
    val userAccesssor: Future[TestUserAccessor] = connectedKeyspace.materialiseAccessor[TestUserAccessor]

    val pk = UUID.randomUUID
    val mideo = new TestUser(pk, "mideo")


    Await.result(userMapper.map {
      _.save(mideo)
    }, 10 seconds)


    val result: Result[TestUser] = Await.result(userAccesssor.map {
      _.getAll
    }, 5 seconds)


    (result.all().size() > 1) should equal(true)


  }
  it should " create accessor (truncate)" in {
    val futureConnectedTable: Future[ConnectedTable[TestUser, TestUserAccessor]] = connectedKeyspace.materialise[TestUser, TestUserAccessor]


    val pk = UUID.randomUUID
    val mideo = new TestUser(pk, "mideo")


    val result: Result[TestUser] = Await.result(
      for {
        table <- futureConnectedTable
        _ <- Future{table.mapper.save(mideo)}
        _ <- Future{table.accessor.truncate}
      } yield table.accessor.getAll,
      10 seconds)


    result.all().size() should equal(0)


  }


  "materialise repository " should "delete data from repository" in {
    val userMapper: Mapper[TestUser] = Await.result(connectedKeyspace.materialise[TestUser], 5 seconds)

    val pk = UUID.randomUUID
    val mideo = new TestUser(pk, "mideo1")

    userMapper.save(mideo)

    userMapper.delete(pk)

    val actual = userMapper.get(pk)
    actual should be(null)
  }


  "materialise repository " should "update data from repository" in {
    val userMapper: Mapper[TestUser] = Await.result(connectedKeyspace.materialise[TestUser], 5 seconds)

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
