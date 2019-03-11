package com.github.mideo

import com.github.mideo.cassandra.connector.repository.{ConnectedRepository, ConnectedSession, RepositoryMapper}
import com.github.mideo.cassandra.testing.support.ConnectedInMemoryRepository

import scala.concurrent.Await
import concurrent.duration._


package object repositories {

  // if connecting to real database, define cassandra-connector.conf resources directory and user ConnectedSession
  // val c: ConnectedRepository = ConnectedRepository(keyspace="cassandra_connector")
  val connectedRepository: ConnectedRepository = ConnectedInMemoryRepository.connect("cassandra_connector")

  // easier to block, as migrations need to run successfully
  Await.result(connectedRepository.runMigrations("migrations"), 5 minutes)

  // RepoMapper
  val RepositoryMapper: RepositoryMapper = connectedRepository.repositoryMapper

  // ConnectedSession
  val ConnectedSession: ConnectedSession = connectedRepository.connectedSession
}
