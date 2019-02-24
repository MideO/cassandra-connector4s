package com.github.mideo.cassandra.connector.repository

import com.datastax.driver.core.Cluster
import com.github.mideo.cassandra.connector.CassandraConnectorTest
import com.github.mideo.cassandra.connector.configuration.RepositoryConfiguration
import org.apache.cassandra.db.ConsistencyLevel

class ClusterFactoryTest extends CassandraConnectorTest {

  "ClusterConnector" should "connect to cassandra cluster" in {
    val cluster: Cluster = ClusterBuilder.fromConfig(new RepositoryConfiguration).build()
    cluster.getClusterName should equal(ClusterBuilder.getClass.getCanonicalName+".cluster")
    cluster.getConfiguration.getSocketOptions.getConnectTimeoutMillis should equal(1000)
    cluster.getConfiguration.getSocketOptions.getReadTimeoutMillis should equal(5000)
    cluster.getConfiguration.getQueryOptions.getConsistencyLevel.name() should equal(ConsistencyLevel.LOCAL_QUORUM.name())

  }

}
