package com.github.mideo

import com.datastax.driver.core.ConsistencyLevel
import com.github.mideo.cassandra.connector.fluent.Connector
import com.github.mideo.cassandra.connector.repository.ConnectedKeyspace
import com.github.mideo.cassandra.testing.support.{ConnectedInMemoryKeyspace, EmbeddedCassandra}

import scala.concurrent.Await
import scala.concurrent.duration._


package object repository {
  //Not needed it real cassandra is running
  EmbeddedCassandra.startDb

  val CassandraKeyspace: ConnectedKeyspace = Connector
      .keyspace("cassandra_connector")
    .onPort(EmbeddedCassandra.runningPort)
    .withConsistencyLevel(ConsistencyLevel.LOCAL_ONE)
    .withContactPoints(EmbeddedCassandra.getHosts)
    .create()
  // can be run async but we block for this example.
  Await.result(CassandraKeyspace.runMigrations("migrations"), 5 minutes)
}
