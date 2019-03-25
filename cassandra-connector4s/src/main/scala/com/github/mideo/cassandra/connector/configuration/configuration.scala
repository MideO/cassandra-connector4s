package com.github.mideo.cassandra.connector

import com.typesafe.config.{Config, ConfigException, ConfigFactory, ConfigValueFactory}

import scala.collection.JavaConverters._
import scala.collection.mutable

package object configuration {

  trait CassandraConfiguration
    extends Settings
      with Environment {

    val config: Config = _env.get("cassandra.connector.environment.conf") match {
      case None => ConfigFactory.load("cassandra-connector.conf")
      case Some(value) => ConfigFactory.load(s"cassandra-connector.$value.conf")
    }
  }


  private[configuration] abstract class OptionalConfiguration(optionals:Option[String]*) {
    def isDefined:Boolean ={
      optionals.nonEmpty && optionals.forall(_.isDefined)
    }
  }

  private[configuration] trait Environment {
    lazy val _env: Map[String, String] = System.getenv().asScala.toMap
  }


  private[configuration] trait Settings
    extends Environment {
    protected val config: Config

    def get[T](key: String): T = {
      config.getAnyRef(key).asInstanceOf[T]
    }

    def getOptional[T](key: String): Option[T] = {
      try {
        Some(config.getAnyRef(key).asInstanceOf[T])
      } catch {
        case _: ConfigException => None
      }
    }
  }
}
