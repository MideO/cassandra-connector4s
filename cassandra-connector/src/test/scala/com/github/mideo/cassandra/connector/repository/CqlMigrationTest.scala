package com.github.mideo.cassandra.connector.repository

import java.nio.file.{Files, Path, Paths}

import com.datastax.driver.core.Session
import com.github.mideo.cassandra.connector.CassandraConnectorTest
import org.mockito.Mockito._
import uk.sky.cqlmigrate.CqlMigrator

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CqlMigrationTest extends CassandraConnectorTest {
  val cqlDirectory = "foo"
  val TempFolder: Path = Paths.get(this.getClass.getResource(".").getPath, cqlDirectory)

  before {
    Files.deleteIfExists(TempFolder)
    Files.createDirectory(TempFolder)
  }
  after {
    Files.delete(TempFolder)
  }

  "CQLMigrations" should "run migrations" in {
    val session = mock[Session]
    val keyspace = "keyspace"

    val sessionFuture: Future[Session] = Future {
      session
    }
    val migrator: CqlMigrator = mock[CqlMigrator]


    when(session.getLoggedKeyspace).thenReturn(keyspace)
    CqlMigration.run(sessionFuture, cqlDirectory, migrator)

    verify(migrator).migrate(session, keyspace, List(TempFolder).asJava)
  }

}
