package com.github.mideo.cassandra.testing.support

import java.lang.management.ManagementFactory
import java.lang.reflect.Field
import java.util.Objects

import com.datastax.driver.core.{Cluster, ConsistencyLevel}
import com.github.mideo.cassandra.connector.configuration.{ClusterCredentials, ClusterDC, RepositoryConfiguration}
import com.github.mideo.cassandra.connector.repository.{ClusterBuilder, ConnectedRepository, CqlMigration}
import javax.management.ObjectName
import org.apache.cassandra.config.DatabaseDescriptor
import org.apache.cassandra.service.{CassandraDaemon, EmbeddedCassandraService}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper

object ConnectedInMemoryRepository {

  val embeddedCassandra: EmbeddedCassandra.type = EmbeddedCassandra


  def connect(keyspace: String, migrationsResourceDirectory:String = "migrations"): ConnectedRepository = {
    embeddedCassandra.startDb

    val connectedRepository: ConnectedRepository = ConnectedRepository(clusterSupplier(keyspace), keyspace)

    CqlMigration.run(connectedRepository.session.connectAsync, migrationsResourceDirectory)

    connectedRepository
  }

  def clusterSupplier (keyspace:String): () => Cluster = {
    val repoConf = RepositoryConfiguration(
      keyspace,
      ConsistencyLevel.LOCAL_ONE,
      embeddedCassandra.runningPort,
      embeddedCassandra.getHosts)
    () => ClusterBuilder.fromConfig(repoConf).build()
  }
}


private[support] object EmbeddedCassandra {

  def startDb = {
    if (!isRunning) cassandraService.start()
  }

  def stopDb = if (isRunning) {
    daemon.stop()
    val field: Field = classOf[DatabaseDescriptor].getDeclaredField("daemonInitialized")
    field.setAccessible(true)
    field.setBoolean(field, false)
    ManagementFactory.getPlatformMBeanServer.unregisterMBean(ObjectName.getInstance( "org.apache.cassandra.db:type=DynamicEndpointSnitch"))
    ManagementFactory.getPlatformMBeanServer.unregisterMBean(ObjectName.getInstance( "org.apache.cassandra.db:type=EndpointSnitchInfo"))
  }

  def isRunning: Boolean = Objects.nonNull(daemon) && daemon.isNativeTransportRunning

  def runningPort = EmbeddedCassandraServerHelper.getNativeTransportPort

  def getHosts = List(EmbeddedCassandraServerHelper.getHost)

  private[support] val cassandraService = new EmbeddedCassandraService()

  private def daemon: CassandraDaemon = {
    val field: Field = cassandraService.getClass.getDeclaredField("cassandraDaemon")
    field.setAccessible(true)
    field.get(cassandraService).asInstanceOf[CassandraDaemon]
  }
}