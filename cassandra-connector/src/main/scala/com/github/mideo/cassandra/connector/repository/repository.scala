package com.github.mideo.cassandra.connector

import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy
import com.datastax.driver.core.{AtomicMonotonicTimestampGenerator, Cluster, QueryLogger, SocketOptions}
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

  private[repository] object ClusterBuilder extends RepositoryConfiguration {
    private var _builder: Cluster.Builder = Cluster.builder

    def fromConfig: Cluster.Builder = {
      val socketOptions = new SocketOptions()
      socketOptions.setConnectTimeoutMillis(5000)
      socketOptions.setReadTimeoutMillis(5000)
      _builder = _builder
                      .withTimestampGenerator(new AtomicMonotonicTimestampGenerator)
                      .addContactPoints(ContactPoints.toArray: _*)
                      .withPort(Port)
                      .withSocketOptions(socketOptions)
                      .withClusterName(this.getClass.getCanonicalName + ".cluster")


      if(Credentials.isDefined) _builder.withCredentials(Credentials.username.get, Credentials.password.get)

      if(ShareDC.isDefined) _builder.withLoadBalancingPolicy(DCAwareRoundRobinPolicy.builder()
                                                              .allowRemoteDCsForLocalConsistencyLevel()
                                                              .withLocalDc(ShareDC.name.get)
                                                              .build())
      _builder
    }


    def build: Cluster = _builder.build().register(QueryLogger.builder().withConstantThreshold(300).build())
  }


}
