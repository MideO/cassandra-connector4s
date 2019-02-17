package com.github.mideo.cassandra.connector.embeddedCassandra

import java.lang.reflect.Field

import org.apache.cassandra.service.{CassandraDaemon, EmbeddedCassandraService}

object EmbeddedCassandra {
  private[embeddedCassandra] val cassandraService = new EmbeddedCassandraService()
  private[embeddedCassandra] def daemon: CassandraDaemon = {
    val field: Field = cassandraService.getClass.getDeclaredField("cassandraDaemon")
    field.setAccessible(true)
    field.get(cassandraService).asInstanceOf[CassandraDaemon]
  }

  def startDb = {
    cassandraService.start()

  }

  def stopDb = {
    if (daemon.isNativeTransportRunning) daemon.stop()
  }
}
//
//def checkState (expression: Boolean, @Nullable errorMessage: Any): Unit = {
//  if (! (expression) ) {
//  throw new IllegalStateException (String.valueOf (errorMessage) )
//}
//}

//import com.google.common.base.Throwables
//import org.apache.cassandra.service.StorageService
//import org.cassandraunit.utils.EmbeddedCassandraServerHelper
//
//class EmbeddedCassandra {
//  private var running = false
//
//  def start(): Unit = {
//    checkState(!running, "cannot start: already running")
//    try {
//      EmbeddedCassandraServerHelper.startEmbeddedCassandra("embedded-cassandra.yml", 30000L)
//      StorageService.instance.removeShutdownHook()
//      running = true
//    } catch {
//      case e: Exception =>
//        throw Throwables.propagate(e)
//    }
//  }
//
//  def isRunning: Boolean = running
//
//  def getPort: Int = {
//    checkState(running, "cannot get port: not running")
//    EmbeddedCassandraServerHelper.getNativeTransportPort
//  }
//}