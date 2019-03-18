package com.github.mideo.cassandra.connector

import com.datastax.driver.core._
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy
import com.github.mideo.cassandra.connector.configuration.{ClusterCredentials, ClusterDC, RepositoryConfiguration, RepositoryConfigurationFromConfigFile}

package object repository {

  private[cassandra] object ClusterBuilder {

    private final val _builder: Cluster.Builder = Cluster.builder
    private final val socketOptions: SocketOptions = new SocketOptions()
    socketOptions.setConnectTimeoutMillis(1000)
    socketOptions.setReadTimeoutMillis(5000)
    private final val clusterName: String = this.getClass.getCanonicalName + ".cluster"


    sealed implicit class PimpedClusterBuilder(builder: Cluster.Builder) {
      def withCredentialsIfPresent(credentials: ClusterCredentials): Cluster.Builder = {
        if (credentials.isDefined) _builder.withCredentials(credentials.username.get, credentials.password.get)
        _builder
      }

      def withDCAwareRoundRobinPolicyIfPresent(clusterDC: ClusterDC): Cluster.Builder = {
        if (clusterDC.isDefined) _builder.withLoadBalancingPolicy(DCAwareRoundRobinPolicy.builder()
          .allowRemoteDCsForLocalConsistencyLevel()
          .withLocalDc(clusterDC.name.get)
          .build())
        _builder
      }
    }

    def fromConfig(repoConf: RepositoryConfiguration = RepositoryConfigurationFromConfigFile()): this.type = {
      _builder.withTimestampGenerator(new AtomicMonotonicTimestampGenerator)
        .addContactPoints(repoConf.contactPoints.toArray: _*)
        .withPort(repoConf.port)
        .withSocketOptions(socketOptions)
        .withQueryOptions(new QueryOptions().setConsistencyLevel(repoConf.consistencyLevel))
        .withClusterName(clusterName)
        .withCredentialsIfPresent(repoConf.credentials)
        .withDCAwareRoundRobinPolicyIfPresent(repoConf.clusterDC)
      this
    }

    def build(): Cluster = _builder.build().register(QueryLogger.builder().withConstantThreshold(300).build())
  }


}
