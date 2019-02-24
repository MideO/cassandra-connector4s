package com.github.mideo.cassandra.connector.testing.support

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


  it should "connect to embeddedCassandra" in {
    val connected: ConnectedRepository = ConnectedInMemoryRepository.connect("keyspace", migrationsResourceDirectory)
    ConnectedInMemoryRepository.embeddedCassandra.isRunning should be(true)
    connected.session should not be null
  }

}
