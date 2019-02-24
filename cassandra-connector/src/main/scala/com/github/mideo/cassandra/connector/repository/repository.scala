package com.github.mideo.cassandra.connector

import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy
import com.datastax.driver.core._
import com.github.mideo.cassandra.connector.configuration.{
  RepositoryConfiguration,
  RepositoryConfigurationFromConfigFile
}

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

package object repository {
  private[connector] object ClusterBuilder {
    private var _builder: Cluster.Builder = Cluster.builder
    private final val socketOptions: SocketOptions = new SocketOptions()
    socketOptions.setConnectTimeoutMillis(1000)
    socketOptions.setReadTimeoutMillis(5000)
    private final val clusterName: String = this.getClass.getCanonicalName + ".cluster"


    def fromConfig(repoConf:RepositoryConfiguration = RepositoryConfigurationFromConfigFile()): Cluster.Builder = {
      _builder = _builder.withTimestampGenerator(new AtomicMonotonicTimestampGenerator)
        .addContactPoints(repoConf.contactPoints.toArray: _*)
        .withPort(repoConf.port)
        .withSocketOptions(socketOptions)
          .withQueryOptions(new QueryOptions().setConsistencyLevel(repoConf.consistencyLevel))
        .withClusterName(clusterName)
      setUpOptionalConfigurations(repoConf)
    }

    def build: Cluster = _builder.build().register(QueryLogger.builder().withConstantThreshold(300).build())

    private def setUpOptionalConfigurations(repoConf:RepositoryConfiguration): Cluster.Builder ={
      if (repoConf.credentials.isDefined) _builder.withCredentials(repoConf.credentials.username.get, repoConf.credentials.password.get)
      if (repoConf.clusterDC.isDefined) _builder.withLoadBalancingPolicy(DCAwareRoundRobinPolicy.builder()
        .allowRemoteDCsForLocalConsistencyLevel()
        .withLocalDc(repoConf.clusterDC.name.get)
        .build())
      _builder
    }
  }

  private[repository] implicit class PimpedJavaFuture[T](jFuture: java.util.concurrent.Future[T]) {
    @tailrec final def asScala: Future[T] = {
      if (jFuture.isDone || jFuture.isCancelled) return Future {
        jFuture.get()
      }
      Thread.sleep(50)
      asScala
    }
  }
}
