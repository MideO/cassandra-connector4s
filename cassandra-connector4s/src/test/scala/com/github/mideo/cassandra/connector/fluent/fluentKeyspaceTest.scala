package com.github.mideo.cassandra.connector.fluent

import com.datastax.driver.core.ConsistencyLevel
import com.github.mideo.cassandra.connector.CassandraConnectorTest

class fluentKeyspaceTest extends CassandraConnectorTest {

  "fluentKeyspace" should "provide connectedKeyspace" in {
    Keyspace.name("keyspace" )
      .withUserName("mideo")
      .withPassword("password")
      .withConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
      .withContactPoints(List("localhost"))
      .onPort(9402)
      .withDC("DC1")
      .connect() should not be None
  }

  "fluentKeyspace" should "provide connectedKeyspace without credentials" in {
    Keyspace.name("keyspace" )
      .withConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
      .withContactPoints(List("localhost"))
      .onPort(9402)
      .withDC("dc-eu-west-1")
      .connect() should not be None
  }

  "fluentKeyspace" should "provide connectedKeyspace without credentials nor DC" in {
    Keyspace.name("keyspace" )
      .withConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
      .withContactPoints(List("localhost"))
      .onPort(9402)
      .connect() should not be None
  }

}
