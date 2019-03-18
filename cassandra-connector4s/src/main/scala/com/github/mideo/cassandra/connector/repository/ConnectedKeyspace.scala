package com.github.mideo.cassandra.connector.repository

import com.datastax.driver.core.{Cluster, Session}
import com.datastax.driver.mapping.{Mapper, MappingManager}

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.reflect._


sealed class ConnectedKeyspace(private val cluster: Cluster, private val keyspace: String) {

  private[repository] implicit class PimpedJavaFuture[T](jFuture: java.util.concurrent.Future[T]) {
    @tailrec final def asScala: Future[T] = {
      if (jFuture.isDone || jFuture.isCancelled) return Future {
        jFuture.get()
      }
      Thread.sleep(50)
      asScala
    }
  }

  lazy val Session: Future[Session] = cluster.connectAsync().asScala

  lazy val Manager: Future[MappingManager] = Session flatMap { s =>
    s.executeAsync(s"USE $keyspace")
      .asScala map {
      _ => new MappingManager(s)
    }
  }

  def close: Future[Void] = cluster.closeAsync().asScala

  def runMigrations(migrationsDirector: String): Future[Unit] = Session map { session => Migrations.migrate(session, keyspace, migrationsDirector) }

  def materialise[T: ClassTag]: Future[Mapper[T]] = Manager map { _.mapper(classTag[T].runtimeClass.asInstanceOf[Class[T]], keyspace)}

  def materialiseAccessor[T: ClassTag]: Future[T] = Manager map {_.createAccessor(classTag[T].runtimeClass.asInstanceOf[Class[T]])}
}

object ConnectedKeyspace {
  def apply(cluster: Cluster = ClusterBuilder.fromConfig().build(), keyspace: String): ConnectedKeyspace = {
    new ConnectedKeyspace(cluster, keyspace)
  }
}

