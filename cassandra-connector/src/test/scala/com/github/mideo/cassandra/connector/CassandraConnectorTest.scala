package com.github.mideo.cassandra.connector

import java.nio.file.{
  Path,
  Paths
}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{
  BeforeAndAfter,
  FlatSpec
  , Matchers}

trait CassandraConnectorTest
  extends FlatSpec
    with BeforeAndAfter
    with MockitoSugar
    with Matchers {
  val migrationsResourceDirectory = "migrations"
  val migrationsDirectoryLocation: Path = Paths.get(this.getClass.getClassLoader.getResource(".").getPath, migrationsResourceDirectory)
}
