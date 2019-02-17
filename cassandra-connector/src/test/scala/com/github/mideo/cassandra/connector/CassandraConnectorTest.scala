package com.github.mideo.cassandra.connector

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpec, Matchers}

trait CassandraConnectorTest
  extends FlatSpec
    with BeforeAndAfter
    with BeforeAndAfterAll
    with MockitoSugar
    with Matchers
