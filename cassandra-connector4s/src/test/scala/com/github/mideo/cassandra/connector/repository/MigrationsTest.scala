package com.github.mideo.cassandra.connector.repository

import java.nio.file.Files

import com.datastax.driver.core.Session
import com.github.mideo.cassandra.connector.CassandraConnectorTest
import com.github.mideo.cassandra.connector.repository.Migrations.CqlMigrationException
import org.mockito.Mockito._
import uk.sky.cqlmigrate.CqlMigrator

import scala.collection.JavaConverters._

class MigrationsTest extends CassandraConnectorTest {
  override def beforeAll() {
    if (!Files.exists(migrationsDirectoryLocation)) Files.createDirectory(migrationsDirectoryLocation)
  }

  private val session = mock[Session]
  private val migrator: CqlMigrator = mock[CqlMigrator]

  val keyspace = "keyspace"

  "CQLMigrations" should "run migrations" in {
    Migrations.migrate(session, keyspace, migrationsResourceDirectory, migrator)

    verify(migrator).migrate(session, keyspace, List(migrationsDirectoryLocation).asJava)
  }

  "CQLMigrations" should "not run  migrations if path does not exist" in {
    the[CqlMigrationException] thrownBy {
      Migrations.migrate(session, keyspace, "abc", migrator)
    } should have message "Resource path `abc` does not exist"
  }

}

