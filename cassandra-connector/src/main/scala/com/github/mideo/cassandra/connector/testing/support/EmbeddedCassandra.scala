package com.github.mideo.cassandra.connector.testing.support

import java.lang.reflect.Field

import org.apache.cassandra.service.{CassandraDaemon, EmbeddedCassandraService}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper

object EmbeddedCassandra {

  def startDb = cassandraService.start()

  def isRunning: Boolean = daemon.isNativeTransportRunning

  def stopDb = if (daemon.isNativeTransportRunning) daemon.stop()

  def runningPort = EmbeddedCassandraServerHelper.getNativeTransportPort

  private[embeddedCassandra] val cassandraService = new EmbeddedCassandraService()

  private def daemon: CassandraDaemon = {
    val field: Field = cassandraService.getClass.getDeclaredField("cassandraDaemon")
    field.setAccessible(true)
    field.get(cassandraService).asInstanceOf[CassandraDaemon]
  }
}
