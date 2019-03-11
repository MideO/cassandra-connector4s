package com.github.mideo.cassandra.connector

import java.nio.file.{Path, Paths}
import java.util.UUID

import com.datastax.driver.mapping.Result
import com.datastax.driver.mapping.annotations._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.beans.BeanProperty

trait CassandraConnectorTest
  extends FlatSpec
    with BeforeAndAfterAll
    with MockitoSugar
    with Matchers {
  val migrationsResourceDirectory = "migrations"
  val migrationsDirectoryLocation: Path = Paths.get(this.getClass.getClassLoader.getResource(".").getPath, migrationsResourceDirectory)
}

@Table(keyspace = "cassandra_connector", name = "users", caseSensitiveKeyspace = false, caseSensitiveTable = false)
class TestUser() {
  @PartitionKey
  @Column(name = "user_id")
  @BeanProperty var userId: UUID = _
  @BeanProperty var name: String = _

  def this(userId: UUID, name: String) = {
    this()
    this.userId = userId
    this.name = name
  }
}


@Accessor trait TestUserAccessor {
  @Query("SELECT * FROM users") def getAll: Result[TestUser]
  @Query("TRUNCATE users") def truncate: Result[TestUser]
}


@Table(keyspace = "cassandra_connector", name = "address", caseSensitiveKeyspace = false, caseSensitiveTable = false)
class TestAddress() {
  @PartitionKey
  @Column(name = "address_id")
  @BeanProperty var addressId: UUID = _
  @BeanProperty var name: String = _

  def this(addressId: UUID, name: String) = {
    this()
    this.addressId = addressId
    this.name = name
  }
}
