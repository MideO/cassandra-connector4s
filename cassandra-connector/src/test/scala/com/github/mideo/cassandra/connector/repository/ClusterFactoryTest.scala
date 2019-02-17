package com.github.mideo.cassandra.connector.repository

import com.datastax.driver.core.Cluster
import com.github.mideo.cassandra.connector.CassandraConnectorTest

class ClusterFactoryTest extends CassandraConnectorTest {

  "ClusterConnector" should "connect to cassandra cluster" in {
    val cluster: Cluster = ClusterBuilder.fromConfig.build()
    cluster.getClusterName should equal("com.github.mideo.cassandra.connector.repository.package.ClusterBuilder$.cluster")
  }

}
