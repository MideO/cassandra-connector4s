package com.github.mideo.cassandra.connector.testing.support

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
    EmbeddedCassandra.isRunning should be(true)
  }

  it should "start EmbeddedDb on port 9402" in {
    EmbeddedCassandra.runningPort should equal(9042)
  }

  it should "stop EmbeddedDb" in {
    EmbeddedCassandra.stopDb
    EmbeddedCassandra.isRunning should be(false)
  }

}
