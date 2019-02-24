package com.github.mideo.cassandra.connector.repository

import java.nio.file.{Path, Paths}
import java.time.Duration
import java.util.Objects

import com.datastax.driver.core.Session
import uk.sky.cqlmigrate.{CassandraLockConfig, CqlMigrator, CqlMigratorFactory}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CqlMigration {

  case class CqlMigrationException(private val message: String = "",
                                   private val cause: Throwable = None.orNull) extends RuntimeException(message, cause)

  lazy val cqlMigrator: CqlMigrator = {
    val LockConfig: CassandraLockConfig = CassandraLockConfig
      .builder
      .withTimeout(Duration.ofSeconds(3))
      .withPollingInterval(Duration.ofMillis(500))
      .build
    CqlMigratorFactory.create(LockConfig)
  }

  def run(futureSession: Future[Session], resourcePath: String, migrator: CqlMigrator = cqlMigrator): Unit = {
    val url = this.getClass.getClassLoader.getResource(resourcePath)
    if (Objects.isNull(url)) throw CqlMigrationException(s"Resource path `$resourcePath` does not exist")

    val paths: Path = Paths.get(url.toURI)
    futureSession map (session => {
      migrator.migrate(session, session.getLoggedKeyspace, List(paths).asJava)
    })
  }
}
