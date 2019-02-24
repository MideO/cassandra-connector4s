package com.github.mideo.cassandra.connector.configuration

import com.datastax.driver.core.ConsistencyLevel

private[connector] case class ClusterCredentials(username: Option[String], password: Option[String]) extends OptionalConfiguration(username, password)

private[connector] case class ClusterDC(name: Option[String]) extends OptionalConfiguration(name)

case class RepositoryConfiguration(credentials: ClusterCredentials,
                                   clusterDC: ClusterDC,
                                   keyspace: String,
                                   consistencyLevel: ConsistencyLevel,
                                   port: Int,
                                   contactPoints: List[String])



object RepositoryConfigurationFromConfigFile extends CassandraConfiguration {
  def apply(): RepositoryConfiguration =
    RepositoryConfiguration(
      ClusterCredentials(getOptional[String]("cassandra-connector.cluster.username"), getOptional[String]("cassandra-connector.cluster.password")),
      ClusterDC(getOptional[String]("cassandra-connector.cluster.dc")),
      get[String]("cassandra-connector.cluster.keyspace"),
      ConsistencyLevel.valueOf(get[String]("cassandra-connector.session.consistencyLevel").toUpperCase),
      get[Int]("cassandra-connector.cluster.port"),
      get[String]("cassandra-connector.cluster.contactPoints").split(",").toList)

}

