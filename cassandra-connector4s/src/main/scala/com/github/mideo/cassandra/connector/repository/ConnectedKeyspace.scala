package com.github.mideo.cassandra.connector.repository

import com.datastax.driver.core.{Cluster, Session}
import com.datastax.driver.mapping.{Mapper, MappingManager}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._


sealed class ConnectedKeyspace(private val cluster: Cluster, private val keyspace: String) {

  lazy val Session: Future[Session] = cluster.connectAsync().asScala

  lazy val Manager: Future[MappingManager] = Session flatMap  { s =>
    s.executeAsync(s"USE $keyspace")
      .asScala map {
      _ => new MappingManager(s)
    }
  }

  def close: Future[Void] = cluster.closeAsync().asScala

  def runMigrations(migrationsDirector:String): Future[Unit] = Session map { session => CqlMigration.run(session, keyspace, migrationsDirector) }

  def materialise[T](t: Class[T]): Future[Mapper[T]] = Manager map { m => m.mapper(t, keyspace) }

  def materialiseAccessor[T](t: Class[T]): Future[T] = Manager map { m => m.createAccessor(t) }

}

object ConnectedKeyspace {
  private def defaultClusterSupplier: () => Cluster = () => ClusterBuilder.fromConfig().build()
  def apply(clusterSupplier: () => Cluster = defaultClusterSupplier, keyspace: String): ConnectedKeyspace = {
    new ConnectedKeyspace(clusterSupplier(), keyspace)
  }
}

