package com.github.mideo.cassandra.connector

import java.nio.file.{Path, Paths}

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpec, Matchers}

trait CassandraConnectorTest
  extends FlatSpec
    with BeforeAndAfter
    with BeforeAndAfterAll
    with MockitoSugar
    with Matchers {
  val migrationsResourceDirectory = "foo"
  val TempFolder: Path = Paths.get(this.getClass.getClassLoader.getResource(".").getPath, migrationsResourceDirectory)
}
