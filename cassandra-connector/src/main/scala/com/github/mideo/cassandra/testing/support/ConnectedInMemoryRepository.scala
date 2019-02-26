package com.github.mideo.cassandra.testing.support

import java.lang.reflect.Field
import java.util.Objects

import com.datastax.driver.core.{Cluster, ConsistencyLevel}
import com.github.mideo.cassandra.connector.configuration.RepositoryConfiguration
import com.github.mideo.cassandra.connector.repository.{ClusterBuilder, ConnectedRepository, CqlMigration}
import org.apache.cassandra.service.{CassandraDaemon, EmbeddedCassandraService}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper

import scala.concurrent.Await
import scala.concurrent.duration._

object ConnectedInMemoryRepository {

  def connect(keyspace: String, migrationsResourceDirectory: String = "migrations"): ConnectedRepository = {
    EmbeddedCassandra.startDb
    val connectedRepository: ConnectedRepository = ConnectedRepository(clusterSupplier(keyspace), keyspace)
    val session = Await.result(connectedRepository.connectedSession.session, Duration.Inf)
    CqlMigration.run(session, keyspace, migrationsResourceDirectory)
    connectedRepository
  }

  def clusterSupplier(keyspace: String): () => Cluster = {
    val repoConf = RepositoryConfiguration(
      keyspace,
      ConsistencyLevel.LOCAL_ONE,
      EmbeddedCassandra.runningPort,
      EmbeddedCassandra.getHosts)
    () => ClusterBuilder.fromConfig(repoConf).build()
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

