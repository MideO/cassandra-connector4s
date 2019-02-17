package com.github.mideo.cassandra.connector.embeddedCassandra

import com.github.mideo.cassandra.connector.CassandraConnectorTest

class EmbeddedCassandraTest extends CassandraConnectorTest {
  behavior of "EmbeddedCassandra"

  override def beforeAll () {
    EmbeddedCassandra.startDb
  }

  override def afterAll() {
    EmbeddedCassandra.stopDb
  }
  it should "start EmbeddedDb" in {
    EmbeddedCassandra.daemon.isNativeTransportRunning should be(true)


  }

  it should "stop EmbeddedDb" in {
    EmbeddedCassandra.stopDb
    EmbeddedCassandra.daemon.isNativeTransportRunning should be(false)
  }

}
