package com.github.mideo.cassandra.connector.repository

import java.nio.file.{Path, Paths}
import java.time.Duration
import java.util.Objects

import com.datastax.driver.core.Session
import uk.sky.cqlmigrate.{CassandraLockConfig, CqlMigrator, CqlMigratorFactory}

import scala.collection.JavaConverters._

private [cassandra] object Migrations {

  case class CqlMigrationException(private val message: String = "",
                                   private val cause: Throwable = None.orNull) extends RuntimeException(message, cause)

  lazy val migrator: CqlMigrator = {
    val LockConfig: CassandraLockConfig = CassandraLockConfig
      .builder
      .withTimeout(Duration.ofSeconds(3))
      .withPollingInterval(Duration.ofMillis(500))
      .build
    CqlMigratorFactory.create(LockConfig)
  }

  def migrate(session: Session, keyspace:String, resourcePath: String, migrator: CqlMigrator = migrator): Unit = {
    val url = this.getClass.getClassLoader.getResource(resourcePath)
    if (Objects.isNull(url)) throw CqlMigrationException(s"Resource path `$resourcePath` does not exist")

    val paths: Path = Paths.get(url.toURI)
    session.execute("CREATE KEYSPACE IF NOT EXISTS cqlmigrate WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1 };")
    session.execute("CREATE TABLE IF NOT EXISTS cqlmigrate.locks (name text PRIMARY KEY, client text);")
    migrator.migrate(session, keyspace, List(paths).asJava)

  }
}
