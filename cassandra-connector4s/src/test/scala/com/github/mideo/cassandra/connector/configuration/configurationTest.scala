package com.github.mideo.cassandra.connector.configuration

import com.github.mideo.cassandra.connector.CassandraConnectorTest
import com.typesafe.config.ConfigException


class configurationTest extends CassandraConnectorTest {
  case class TestOptionalConfiguration(abs:Option[String], xyz:Option[String]) extends OptionalConfiguration(abs, xyz)

  private object TestAppSettings extends CassandraConfiguration  {
    override lazy val _env: Map[String, String] = Map("foo" -> "bar", "cassandra.connector.environment.conf" -> "dev")
  }

  it should "throw ConfigException when value is not set" in {

    the[ConfigException] thrownBy {
      TestAppSettings.get[String]("lala") should equal(None)
    } should have message "No configuration setting found for key 'lala'"
  }

  it should "return none when value is not set" in {
    TestAppSettings.getOptional[String]("lala") should equal(None)

  }

  it should "not create unknown config value" in {
    TestAppSettings.getOptional[String]("foo") should equal(None)
  }


  it should "update config with production environment" in {
    TestAppSettings.get[String]("cassandra-connector.cluster.username") should equal("dev")
  }


  it should "return optional config value" in {
    TestAppSettings.getOptional[String]("cassandra-connector.cluster.username") should equal(Some("dev"))
  }

  it should "return true when OptionalConfiguration is defined" in {
    TestOptionalConfiguration(Some("assc"), Some("abc")).isDefined should be (true)
  }

  it should "return false when OptionalConfiguration not fully defined" in {
    TestOptionalConfiguration(None, Some("abc")).isDefined should be (false)
  }

  it should "return false when OptionalConfiguration not  defined" in {
    TestOptionalConfiguration(None, None).isDefined should be (false)
  }
}
