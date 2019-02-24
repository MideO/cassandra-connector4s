package com.github.mideo.cassandra.testing.support

import java.nio.file.Files

import com.github.mideo.cassandra.connector.CassandraConnectorTest
import com.github.mideo.cassandra.connector.repository.ConnectedRepository

class ConnectedInMemoryRepositoryTest extends CassandraConnectorTest {

  behavior of "ConnectedInMemoryRepositoryTest"

  before {
    Files.deleteIfExists(TempFolder)
    Files.createDirectory(TempFolder)
  }
  after {
    Files.deleteIfExists(TempFolder)
  }

  lazy val connected: ConnectedRepository = ConnectedInMemoryRepository.connect("keyspace", migrationsResourceDirectory)


  it should "start EmbeddedDb" in {
    connected.session should not be null
    ConnectedInMemoryRepository.embeddedCassandra.isRunning should be(true)
  }

  it should "start EmbeddedDb one when called multiple times" in {
    ConnectedInMemoryRepository.embeddedCassandra.startDb
    ConnectedInMemoryRepository.embeddedCassandra.startDb
    ConnectedInMemoryRepository.embeddedCassandra.isRunning should be(true)
  }

  it should "start EmbeddedDb on port 9402" in {
    ConnectedInMemoryRepository.embeddedCassandra.runningPort should equal(9042)
  }

  it should "start EmbeddedDb on localhost" in {
    ConnectedInMemoryRepository.embeddedCassandra.getHosts should equal(List("localhost"))
  }

  it should "stop EmbeddedDb" in {
    ConnectedInMemoryRepository.embeddedCassandra.stopDb
    ConnectedInMemoryRepository.embeddedCassandra.isRunning should be(false)
  }


}
