package com.github.mideo.cassandra.connector.testing.support

import java.lang.reflect.Field
import java.util.Objects

import org.apache.cassandra.service.{CassandraDaemon, EmbeddedCassandraService}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper

private[support] object SimpleEmbeddedCassandra {

  def startDb = if(!isRunning) cassandraService.start()

  def stopDb =  if(isRunning) daemon.stop()

  def isRunning: Boolean = Objects.nonNull(daemon) && daemon.isNativeTransportRunning

  def runningPort = EmbeddedCassandraServerHelper.getNativeTransportPort

  def getHosts = List(EmbeddedCassandraServerHelper.getHost)

  private[support] val cassandraService = new EmbeddedCassandraService()

  private def daemon: CassandraDaemon = {
    val field: Field = cassandraService.getClass.getDeclaredField("cassandraDaemon")
    field.setAccessible(true)
    field.get(cassandraService).asInstanceOf[CassandraDaemon]
  }
}
