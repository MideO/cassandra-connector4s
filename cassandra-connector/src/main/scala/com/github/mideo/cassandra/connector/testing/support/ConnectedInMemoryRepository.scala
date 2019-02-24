package com.github.mideo.cassandra.connector.testing.support

import com.datastax.driver.core.ConsistencyLevel
import com.github.mideo.cassandra.connector.configuration.{ClusterCredentials, ClusterDC, RepositoryConfiguration}
import com.github.mideo.cassandra.connector.repository.{ClusterBuilder, ConnectedRepository, CqlMigration}

object ConnectedInMemoryRepository {

  val embeddedCassandra: SimpleEmbeddedCassandra.type = SimpleEmbeddedCassandra


  def connect(keyspace: String, migrationsResourceDirectory:String = "migrations"): ConnectedRepository = {
    if(!embeddedCassandra.isRunning) embeddedCassandra.startDb

    val repoConf = RepositoryConfiguration(ClusterCredentials(None, None),
      ClusterDC(None),
      keyspace,
      ConsistencyLevel.LOCAL_ONE,
      embeddedCassandra.runningPort,
      embeddedCassandra.getHosts)
    val connectedRepository: ConnectedRepository = ConnectedRepository(() => ClusterBuilder.fromConfig(repoConf).build(), keyspace)
    CqlMigration.run(connectedRepository.session.connectAsync, migrationsResourceDirectory)
    connectedRepository
  }
}
