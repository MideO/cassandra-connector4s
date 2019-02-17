package com.github.mideo.cassandra.connector.repository

import com.datastax.driver.core.{Cluster, Session}
import com.datastax.driver.mapping.{Mapper, MappingManager}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


sealed class ConnectedRepository(private val cluster: Cluster,
                                 keyspace: String = ClusterBuilder.Keyspace) {
  lazy val session: ConnectedSession = ConnectedSession(cluster, keyspace)
  lazy val repositoryMapper: RepositoryMapper = RepositoryMapper(session.get)
}

object ConnectedRepository {
  def apply(clusterSupplier: () => Cluster = () => ClusterBuilder.fromConfig.build(), keyspace: String = ClusterBuilder.Keyspace): ConnectedRepository = {
    new ConnectedRepository(clusterSupplier(), keyspace)
  }
}

case class ConnectedSession(private val cluster: Cluster, private val keyspace: String) {
  def get: Future[Session] = cluster.connectAsync(keyspace).asScala

  def close: Future[Void] = {
    cluster.closeAsync().asScala
  }
}

case class RepositoryMapper(private val session: Future[Session]) {
  private val manager: Future[MappingManager] = session map { s => new MappingManager(s) }

  def materialise[T](t: Class[T]): Future[Mapper[T]] = manager map { m => m.mapper(t) }
}
