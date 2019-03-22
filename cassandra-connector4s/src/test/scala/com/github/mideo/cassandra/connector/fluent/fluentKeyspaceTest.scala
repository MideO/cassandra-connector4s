package com.github.mideo.cassandra.connector.fluent

import com.datastax.driver.core.ConsistencyLevel
import com.github.mideo.cassandra.connector.CassandraConnectorTest

class fluentKeyspaceTest extends CassandraConnectorTest {

  "fluentKeyspace" should "provide connectedKeyspace" in {
    Connector.keyspace("keyspace" )
      .withUserName("mideo")
      .withPassword("password")
      .withConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
      .withContactPoints(List("localhost"))
      .onPort(9402)
      .withDC("DC1")
      .create() should not be None
  }

  "fluentKeyspace" should "provide connectedKeyspace without credentials" in {
    Connector.keyspace("keyspace" )
      .withConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
      .withContactPoints(List("localhost"))
      .onPort(9402)
      .withDC("dc-eu-west-1")
      .create() should not be None
  }

  "fluentKeyspace" should "provide connectedKeyspace without credentials nor DC" in {
    Connector.keyspace("keyspace" )
      .withConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
      .withContactPoints(List("localhost"))
      .onPort(9402)
      .create() should not be None
  }

}
