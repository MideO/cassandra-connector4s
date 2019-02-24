package com.github.mideo.cassandra.connector.repository

import java.nio.file.{Files, Path, Paths}

import com.datastax.driver.core.Session
import com.github.mideo.cassandra.connector.CassandraConnectorTest
import com.github.mideo.cassandra.connector.repository.CqlMigration.CqlMigrationException
import org.mockito.Mockito._
import uk.sky.cqlmigrate.CqlMigrator

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CqlMigrationTest extends CassandraConnectorTest {
  before {
    Files.deleteIfExists(TempFolder)
    Files.createDirectory(TempFolder)
  }
  after {
    Files.delete(TempFolder)
  }
  private val session = mock[Session]
  private val sessionFuture: Future[Session] = Future {
    session
  }
  private val migrator: CqlMigrator = mock[CqlMigrator]

  "CQLMigrations" should "run migrations" in {
    val keyspace = "keyspace"
    when(session.getLoggedKeyspace).thenReturn(keyspace)

    CqlMigration.run(sessionFuture, migrationsResourceDirectory, migrator)

    verify(migrator).migrate(session, keyspace, List(TempFolder).asJava)
  }

  "CQLMigrations" should "not run  migrations if path does not exist" in {
    the[CqlMigrationException] thrownBy {
      CqlMigration.run(sessionFuture, "abc", migrator)
    } should have message "Resource path `abc` does not exist"
  }

}

