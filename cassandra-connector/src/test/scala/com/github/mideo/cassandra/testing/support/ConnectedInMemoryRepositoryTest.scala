package com.github.mideo.cassandra.testing.support

import java.nio.file.Files

import com.github.mideo.cassandra.connector.CassandraConnectorTest

class ConnectedInMemoryRepositoryTest extends CassandraConnectorTest {

  behavior of "ConnectedInMemoryRepositoryTest"

  override def beforeAll {
    Files.deleteIfExists(TempFolder)
    Files.createDirectory(TempFolder)
    ConnectedInMemoryRepository.connect("keyspace", migrationsResourceDirectory)
  }

  override def afterAll {
    Files.deleteIfExists(TempFolder)
  }

  it should "start EmbeddedDb" in {
    ConnectedInMemoryRepository.EmbeddedCassandra.isRunning should be(true)
    ConnectedInMemoryRepository.EmbeddedCassandra.runningPort should not equal null
  }

  it should "have idempotent connect method " in {

    ConnectedInMemoryRepository.connect("keyspace", migrationsResourceDirectory)
    val port =ConnectedInMemoryRepository.EmbeddedCassandra.runningPort
    ConnectedInMemoryRepository.connect("keyspace", migrationsResourceDirectory)
    val newPort = ConnectedInMemoryRepository.EmbeddedCassandra.runningPort
    ConnectedInMemoryRepository.EmbeddedCassandra.isRunning should be(true)
    port should equal(newPort)
  }



  it should "start EmbeddedDb on localhost" in {
    ConnectedInMemoryRepository.EmbeddedCassandra.getHosts should equal(List("localhost"))
  }


}
