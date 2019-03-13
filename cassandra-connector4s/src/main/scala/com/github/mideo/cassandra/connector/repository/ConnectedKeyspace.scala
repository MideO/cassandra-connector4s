package com.github.mideo.cassandra.connector.repository

import com.datastax.driver.core.{Cluster, Session}
import com.datastax.driver.mapping.{Mapper, MappingManager}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


sealed class ConnectedKeyspace(private val cluster: Cluster, private val keyspace: String) {

  lazy val session: Future[Session] = cluster.connectAsync().asScala

  def close: Future[Void] = {
    cluster.closeAsync().asScala
  }
  def runMigrations(migrationsDirector:String): Future[Unit] = {
     session map {
      session => CqlMigration.run(session, keyspace, migrationsDirector)
    }
  }

  private lazy val manager: Future[MappingManager] = session flatMap  { s =>
    s.executeAsync(s"USE $keyspace")
      .asScala map {
      _ => new MappingManager(s)
    }
  }

  def materialise[T](t: Class[T]): Future[Mapper[T]] = manager map { m => m.mapper(t, keyspace) }

  def materialiseAccessor[T](t: Class[T]) = manager map { m => m.createAccessor(t) }

  def getMappingManager: Future[MappingManager] = manager
}

object ConnectedKeyspace {
  private def defaultClusterSupplier: () => Cluster = () => ClusterBuilder.fromConfig().build()
  def apply(clusterSupplier: () => Cluster = defaultClusterSupplier, keyspace: String): ConnectedKeyspace = {
    new ConnectedKeyspace(clusterSupplier(), keyspace)
  }
}

