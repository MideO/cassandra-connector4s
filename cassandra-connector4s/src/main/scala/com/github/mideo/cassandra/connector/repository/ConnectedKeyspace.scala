package com.github.mideo.cassandra.connector.repository

import com.datastax.driver.core.{Cluster, Session}
import com.datastax.driver.mapping.{Mapper, MappingManager}

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.reflect._


sealed class ConnectedKeyspace(private val keyspace: String, private val cluster: Cluster) {

  sealed implicit class PimpedJavaFuture[T](jFuture: java.util.concurrent.Future[T]) {
    @tailrec final def asScala: Future[T] = {
      if (jFuture.isDone || jFuture.isCancelled) return Future {
        jFuture.get()
      }
      Thread.sleep(50)
      asScala
    }
  }

  lazy val Session: Future[Session] = cluster.connectAsync().asScala

  lazy val Manager: Future[MappingManager] = for {
    session <- Session
    _ <- session.executeAsync(s"USE $keyspace").asScala
  } yield new MappingManager(session)


  def close: Future[Void] = cluster.closeAsync().asScala

  def runMigrations(migrationsDirector: String): Future[Unit] = Session map { session => Migrations.migrate(session, keyspace, migrationsDirector) }

  def materialise[T: ClassTag]: Future[Mapper[T]] = Manager map {
    _.mapper(classTag[T].runtimeClass.asInstanceOf[Class[T]], keyspace)
  }

  def materialiseAccessor[T: ClassTag]: Future[T] = Manager map {
    _.createAccessor(classTag[T].runtimeClass.asInstanceOf[Class[T]])
  }

  def materialise[Mapped: ClassTag, Accessor: ClassTag]: Future[ConnectedTable[Mapped, Accessor]] =
    Manager map {
      m =>
        ConnectedTable(
          m.mapper(classTag[Mapped].runtimeClass.asInstanceOf[Class[Mapped]], keyspace),
          m.createAccessor(classTag[Accessor].runtimeClass.asInstanceOf[Class[Accessor]])
        )
    }
}

sealed case class ConnectedTable[T, K](mapper: Mapper[T], accessor: K)

object ConnectedKeyspace {
  def apply(keyspace: String, migrationsDirector: Option[String] = None)(implicit cluster: Cluster = DefaultCluster.fromConfig().build()): Future[ConnectedKeyspace] = for {
    c <- Future {
      new ConnectedKeyspace(keyspace, cluster)
    }
    _ <- if(migrationsDirector.isEmpty) Future {} else c.runMigrations(migrationsDirector.get)
  } yield c
}

