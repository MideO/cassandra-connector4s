package com.github.mideo.cassandra.connector.fluent

import com.datastax.driver.core.{Cluster, ConsistencyLevel}
import com.github.mideo.cassandra.connector.configuration.{ClusterCredentials, ClusterDC, RepositoryConfiguration}
import com.github.mideo.cassandra.connector.repository.{ConnectedKeyspace, DefaultCluster}

import scala.collection.mutable
import scala.concurrent.Future


object Connector {
  private val conf = mutable.Map.empty[String, Any]

  def keyspace(name: String): this.type = {
    conf("keyspace") = name
    this
  }

  def withConsistencyLevel(consistencyLevel: ConsistencyLevel): this.type = {
    conf("consistencyLevel") = consistencyLevel
    this
  }

  def onPort(port: Int): this.type = {
    conf("port") = port
    this
  }

  def withContactPoints(contactPoints: List[String]): this.type = {
    conf("contactPoints") = contactPoints
    this
  }

  def withUserName(username: String): this.type = {
    conf("username") = username
    this
  }

  def withPassword(password: String): this.type = {
    conf("password") = password
    this
  }

  def withDC(dc: String): this.type = {
    conf("dc") = dc
    this
  }

  def withMigrationsDirectory(directory: String): this.type = {
    conf("migrationsDirectory") = directory
    this
  }

  def connect(): Future[ConnectedKeyspace] = {
    val repoConf = RepositoryConfiguration(
      conf("keyspace").asInstanceOf[String],
      conf("consistencyLevel").asInstanceOf[ConsistencyLevel],
      conf("port").asInstanceOf[Int],
      conf("contactPoints").asInstanceOf[List[String]],
      ClusterCredentials(conf.get("username").asInstanceOf[Option[String]], conf.get("password").asInstanceOf[Option[String]]),
      ClusterDC(conf.get("dc").asInstanceOf[Option[String]])
    )
    implicit val cluster: Cluster = DefaultCluster.fromConfig(repoConf).build()
    ConnectedKeyspace(
      conf("keyspace").asInstanceOf[String],
      conf.get("migrationsDirectory").asInstanceOf[Option[String]])

  }
}
