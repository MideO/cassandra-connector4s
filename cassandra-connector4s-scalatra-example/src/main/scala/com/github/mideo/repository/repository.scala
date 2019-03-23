package com.github.mideo

import com.github.mideo.cassandra.connector.repository.ConnectedKeyspace
import com.github.mideo.cassandra.testing.support.ConnectedInMemoryKeyspace

import scala.concurrent.Await
import concurrent.duration._


package object repository {

  // if connecting to real database, define cassandra-connector.conf resources directory and user ConnectedSession
  // val c: ConnectedRepository = ConnectedRepository(keyspace="cassandra_connector")
  val CassandraKeyspace: ConnectedKeyspace = ConnectedInMemoryKeyspace("cassandra_connector")
  // easier to block, as migrations need to run successfully
  Await.result(CassandraKeyspace.runMigrations("migrations"), 5 minutes)
}
