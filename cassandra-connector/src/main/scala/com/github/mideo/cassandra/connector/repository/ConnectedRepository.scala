package com.github.mideo.cassandra.connector.repository

import com.datastax.driver.core.{Cluster, Session}
import com.datastax.driver.mapping.{Mapper, MappingManager}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


sealed class ConnectedRepository(private val cluster: Cluster, private val keyspace: String) {
  lazy val session: ConnectedSession = ConnectedSession(cluster)
  lazy val repositoryMapper: RepositoryMapper = RepositoryMapper(session, keyspace)
}

object ConnectedRepository {
  def apply(clusterSupplier: () => Cluster = () => ClusterBuilder.fromConfig().build(), keyspace: String ): ConnectedRepository = {
    new ConnectedRepository(clusterSupplier(), keyspace)
  }
}

case class ConnectedSession(private val cluster: Cluster) {
  def connectAsync: Future[Session] = cluster.connectAsync().asScala

  def close: Future[Void] = {
    cluster.closeAsync().asScala
  }
}

case class RepositoryMapper(private val session: ConnectedSession, private val keyspace:String) {
  private lazy val manager: Future[MappingManager] = session.connectAsync map { s =>
    s.executeAsync(s"USE $keyspace")
    new MappingManager(s)
  }

  def materialise[T](t: Class[T]): Future[Mapper[T]] = manager map { m => m.mapper(t) }
}
