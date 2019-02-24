package com.github.mideo.cassandra.connector.configuration

import com.github.mideo.cassandra.connector.CassandraConnectorTest
import org.apache.cassandra.db.ConsistencyLevel

class RepositoryConfigurationTest extends CassandraConnectorTest {

  val conf = RepositoryConfigurationFromConfigFile()

  it should "load cassandra settings" in {
    conf.credentials.username.get should equal("user")
    conf.credentials.password.get should equal("pass")
    conf.keyspace should equal("cassandra_connector")
    conf.port should equal(9402)
    conf.contactPoints should equal(List("localhost"))
    conf.consistencyLevel.name() should equal(ConsistencyLevel.LOCAL_QUORUM.name())
  }
}
