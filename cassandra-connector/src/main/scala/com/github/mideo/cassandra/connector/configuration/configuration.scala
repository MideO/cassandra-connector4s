package com.github.mideo.cassandra.connector

import com.typesafe.config.{Config, ConfigException, ConfigFactory, ConfigValueFactory}

import scala.collection.JavaConverters._
import scala.collection.mutable

package object configuration {

  trait CassandraConfiguration
    extends Settings
      with Environment {

    val config: Config = _env.get("cassandra.connector.environment.conf") match {
      case None => updateFromEnvVars(ConfigFactory.load("cassandra-connector.conf"))
      case Some(value) => updateFromEnvVars(ConfigFactory.load(s"cassandra-connector.$value.conf"))
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

    protected def updateFromEnvVars(conf: Config): Config = {
      val temp: mutable.Map[String, Any] = mutable.Map.empty

      conf.entrySet().forEach(
        entry => {
          val _key = entry.getKey
          val sysKey = _key.toUpperCase.replaceAll("(\\.|-)", "_")

          _env.get(sysKey) match {
            case None => temp.put(_key, ConfigValueFactory.fromAnyRef(conf.getAnyRef(_key)))
            case Some(value) => temp.put(_key, ConfigValueFactory.fromAnyRef(value))
          }
        }
      )
      ConfigFactory.parseMap(temp.asJava)
    }
  }


}
