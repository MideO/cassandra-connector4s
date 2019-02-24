package com.github.mideo.cassandra.connector

import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy
import com.datastax.driver.core._
import com.github.mideo.cassandra.connector.configuration.RepositoryConfiguration

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

package object repository {

  private[repository] implicit class PimpedJavaFuture[T](jFuture: java.util.concurrent.Future[T]) {
    @tailrec final def asScala: Future[T] = {
      if (jFuture.isDone || jFuture.isCancelled) return Future {
        jFuture.get()
      }
      Thread.sleep(50)
      asScala
    }
  }

  private[repository] object ClusterBuilder {
    private var _builder: Cluster.Builder = Cluster.builder
    private final val socketOptions: SocketOptions = new SocketOptions()
    socketOptions.setConnectTimeoutMillis(1000)
    socketOptions.setReadTimeoutMillis(5000)
    private final val clusterName: String = this.getClass.getCanonicalName + ".cluster"

    def fromConfig(repoConf:RepositoryConfiguration): Cluster.Builder = {
      _builder = _builder.withTimestampGenerator(new AtomicMonotonicTimestampGenerator)
        .addContactPoints(repoConf.ContactPoints.toArray: _*)
        .withPort(repoConf.Port)
        .withSocketOptions(socketOptions)
          .withQueryOptions(new QueryOptions().setConsistencyLevel(repoConf._ConsistencyLevel))
        .withClusterName(clusterName)
      setUpOptionalConfigurations(repoConf)
    }

    def build: Cluster = _builder.build().register(QueryLogger.builder().withConstantThreshold(300).build())

    private def setUpOptionalConfigurations(repoConf:RepositoryConfiguration): Cluster.Builder ={
      if (repoConf.Credentials.isDefined) _builder.withCredentials(repoConf.Credentials.username.get, repoConf.Credentials.password.get)
      if (repoConf.DC.isDefined) _builder.withLoadBalancingPolicy(DCAwareRoundRobinPolicy.builder()
        .allowRemoteDCsForLocalConsistencyLevel()
        .withLocalDc(repoConf.DC.name.get)
        .build())
      _builder
    }
  }
}
