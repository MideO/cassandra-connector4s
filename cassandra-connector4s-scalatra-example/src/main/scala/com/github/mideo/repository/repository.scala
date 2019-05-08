package com.github.mideo

import com.datastax.driver.core.ConsistencyLevel
import com.github.mideo.cassandra.connector.fluent.Connector
import com.github.mideo.cassandra.connector.repository.ConnectedKeyspace
import com.github.mideo.cassandra.testing.support.EmbeddedCassandra

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


package object repository {
  EmbeddedCassandra.startDb

  val CassandraKeyspace: Future[ConnectedKeyspace] = for {
    c <- Connector
      .keyspace("cassandra_connector")
      .onPort(EmbeddedCassandra.runningPort)
      .withConsistencyLevel(ConsistencyLevel.LOCAL_ONE)
      .withContactPoints(EmbeddedCassandra.getHosts)
      .connect()
    _ <- {
      c.runMigrations("migrations")
    }
  } yield c

}
