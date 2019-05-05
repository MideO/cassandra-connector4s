package com.github.mideo

import com.datastax.driver.core.ConsistencyLevel
import com.github.mideo.cassandra.connector.fluent.Connector
import com.github.mideo.cassandra.connector.repository.ConnectedKeyspace
import com.github.mideo.cassandra.testing.support.EmbeddedCassandra
import com.sun.nio.zipfs.ZipFileSystemProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


package object repository {
  EmbeddedCassandra.startDb

  val CassandraKeyspace: Future[ConnectedKeyspace] = for {
      c <- Future {
        Connector
          .keyspace("cassandra_connector")
          .onPort(EmbeddedCassandra.runningPort)
          .withConsistencyLevel(ConsistencyLevel.LOCAL_ONE)
          .withContactPoints(EmbeddedCassandra.getHosts)
          .connect()
      }
      _ <- {
        val uri =  this.getClass.getClassLoader.getResource("migrations").toURI
        if(new ZipFileSystemProvider().getScheme.equals(uri.getScheme)) {
          import java.nio.file.FileSystems
          import collection.JavaConverters._
          FileSystems.newFileSystem(uri, Map("create" ->"true").asJava)
        }
        c.runMigrations("migrations")
      }
    } yield c

}
