package com.github.mideo.cassandra.connector.configuration

import com.datastax.driver.core.ConsistencyLevel

sealed case class ClusterCredentials(username: Option[String], password: Option[String]) extends OptionalConfiguration(username, password)

sealed case class ClusterDC(name: Option[String]) extends OptionalConfiguration(name)

class RepositoryConfiguration extends CassandraConfiguration {

  lazy val Credentials = ClusterCredentials(
    getOptional[String]("cassandra-connector.cluster.username"),
    getOptional[String]("cassandra-connector.cluster.password"))

  lazy val DC: ClusterDC = ClusterDC(getOptional[String]("cassandra-connector.cluster.dc"))
  lazy val Keyspace: String = get[String]("cassandra-connector.cluster.keyspace")
  lazy val _ConsistencyLevel: ConsistencyLevel = ConsistencyLevel.valueOf(get[String]("cassandra-connector.session.consistencyLevel").toUpperCase)
  lazy val Port: Int = get[Int]("cassandra-connector.cluster.port")
  lazy val ContactPoints: List[String] = get[String]("cassandra-connector.cluster.contactPoints").split(",").toList
}


