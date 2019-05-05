package com.github.mideo.cassandra.connector.repository

import java.net.{URI, URL}
import java.nio.file.Paths
import java.time.Duration
import java.util.Objects

import com.datastax.driver.core.Session
import uk.sky.cqlmigrate.{CassandraLockConfig, CqlMigrator, CqlMigratorFactory}

import scala.collection.JavaConverters._

private[cassandra] object Migrations {

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

  private def handleZipFileSystemProvider(uri: URI): Unit = {
    import com.sun.nio.zipfs.ZipFileSystemProvider
    if (new ZipFileSystemProvider().getScheme.equals(uri.getScheme)) {
      import java.nio.file.FileSystems

      import collection.JavaConverters._
      try {
        FileSystems.newFileSystem(uri, Map("create" -> "true").asJava)
      }
      catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }

  def migrate(session: Session, keyspace: String, resourcePath: String, migrator: CqlMigrator = migrator): Unit = {
    val url: URL = this.getClass.getClassLoader.getResource(resourcePath)
    if (Objects.isNull(url)) throw CqlMigrationException(s"Resource path `$resourcePath` does not exist")

    handleZipFileSystemProvider(url.toURI)
    session.execute("CREATE KEYSPACE IF NOT EXISTS cqlmigrate WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1 };")
    session.execute("CREATE TABLE IF NOT EXISTS cqlmigrate.locks (name text PRIMARY KEY, client text);")
    migrator.migrate(session, keyspace, List(Paths.get(url.toURI)).asJava)

  }
}
