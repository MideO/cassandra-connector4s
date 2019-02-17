package com.github.mideo.cassandra.connector.configuration

import com.github.mideo.cassandra.connector.CassandraConnectorTest
import org.apache.cassandra.db.ConsistencyLevel

class RepositoryConfigurationTest extends CassandraConnectorTest {

  object TestRepositoryConfiguration extends RepositoryConfiguration

  it should "load cassandra settings" in {
    TestRepositoryConfiguration.Credentials.username.get should equal("user")
    TestRepositoryConfiguration.Credentials.password.get should equal("pass")
    TestRepositoryConfiguration.Keyspace should equal("cassandra_connector")
    TestRepositoryConfiguration.Port should equal(9402)
    TestRepositoryConfiguration.ContactPoints should equal(List("localhost"))
    TestRepositoryConfiguration.Consistency.name() should equal(ConsistencyLevel.LOCAL_QUORUM.name())
  }
}
