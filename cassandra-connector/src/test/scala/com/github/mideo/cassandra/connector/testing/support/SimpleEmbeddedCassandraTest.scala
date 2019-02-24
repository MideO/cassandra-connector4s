package com.github.mideo.cassandra.connector.testing.support

import com.github.mideo.cassandra.connector.CassandraConnectorTest

class SimpleEmbeddedCassandraTest extends CassandraConnectorTest {
  behavior of "EmbeddedCassandra"

  override def beforeAll () {
    SimpleEmbeddedCassandra.startDb
  }

  override def afterAll() {
    SimpleEmbeddedCassandra.stopDb
  }
  it should "start EmbeddedDb" in {
    SimpleEmbeddedCassandra.isRunning should be(true)
  }

  it should "start EmbeddedDb one when called multiple times" in {
    SimpleEmbeddedCassandra.startDb
    SimpleEmbeddedCassandra.startDb
    SimpleEmbeddedCassandra.isRunning should be(true)
  }

  it should "start EmbeddedDb on port 9402" in {
    SimpleEmbeddedCassandra.runningPort should equal(9042)
  }

  it should "start EmbeddedDb on localhost" in {
    SimpleEmbeddedCassandra.getHosts should equal(List("localhost"))
  }

  it should "stop EmbeddedDb" in {
    SimpleEmbeddedCassandra.stopDb
    SimpleEmbeddedCassandra.isRunning should be(false)
  }

}
