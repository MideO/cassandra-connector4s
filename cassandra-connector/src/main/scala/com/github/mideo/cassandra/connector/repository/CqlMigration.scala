package com.github.mideo.cassandra.connector.repository

import java.nio.file.{Path, Paths}
import java.time.Duration

import com.datastax.driver.core.Session
import uk.sky.cqlmigrate.{CassandraLockConfig, CqlMigrator, CqlMigratorFactory}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CqlMigration {

  lazy val cqlMigrator: CqlMigrator = {
    val LockConfig: CassandraLockConfig = CassandraLockConfig
      .builder
      .withTimeout(Duration.ofSeconds(3))
      .withPollingInterval(Duration.ofMillis(500))
      .build
    CqlMigratorFactory.create(LockConfig)
  }

  def run(futureSession: Future[Session], resourcePath: String, migrator: CqlMigrator = cqlMigrator): Unit = {
    val paths: Path = Paths.get(this.getClass.getResource(resourcePath).toURI)
    futureSession map (session => {
      migrator.migrate(session, session.getLoggedKeyspace, List(paths).asJava)
    })
  }
}
