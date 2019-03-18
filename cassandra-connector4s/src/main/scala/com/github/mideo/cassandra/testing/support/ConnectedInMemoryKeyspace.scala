package com.github.mideo.cassandra.testing.support

import java.lang.reflect.Field
import java.util.Objects

import com.datastax.driver.core.{Cluster, ConsistencyLevel}
import com.github.mideo.cassandra.connector.configuration.RepositoryConfiguration
import com.github.mideo.cassandra.connector.repository.{ClusterBuilder, ConnectedKeyspace}
import org.apache.cassandra.service.{CassandraDaemon, EmbeddedCassandraService}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper

object ConnectedInMemoryKeyspace {

  def connect(keyspace: String): ConnectedKeyspace = {
    EmbeddedCassandra.startDb
    ConnectedKeyspace(buildCluster(keyspace), keyspace)
  }

  def buildCluster(keyspace: String): Cluster = {
    val repoConf = RepositoryConfiguration(
      keyspace,
      ConsistencyLevel.LOCAL_ONE,
      EmbeddedCassandra.runningPort,
      EmbeddedCassandra.getHosts)
    ClusterBuilder.fromConfig(repoConf).build()
  }

  object EmbeddedCassandra {

    def startDb = EmbeddedCassandraServerHelper.startEmbeddedCassandra(EmbeddedCassandraServerHelper.CASSANDRA_RNDPORT_YML_FILE)

    def isRunning: Boolean = Objects.nonNull(daemon) && daemon.isNativeTransportRunning

    def runningPort = EmbeddedCassandraServerHelper.getNativeTransportPort

    def getHosts = List(EmbeddedCassandraServerHelper.getHost)

    private[support] val cassandraService = new EmbeddedCassandraService()

    private def daemon: CassandraDaemon = {
      val field: Field = classOf[EmbeddedCassandraServerHelper].getDeclaredField("cassandraDaemon")
      field.setAccessible(true)
      field.get(cassandraService).asInstanceOf[CassandraDaemon]
    }
  }

}

