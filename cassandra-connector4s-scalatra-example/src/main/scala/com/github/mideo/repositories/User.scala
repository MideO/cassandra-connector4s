package com.github.mideo.repositories

import java.util.UUID

import com.datastax.driver.mapping.Result
import com.datastax.driver.mapping.annotations._
import org.codehaus.jackson.annotate.JsonIgnore

import scala.beans.BeanProperty

@Table(keyspace = "cassandra_connector", name = "users", caseSensitiveKeyspace = false, caseSensitiveTable = false)
class User() {
  @PartitionKey
  @Column(name = "user_id")
  @JsonIgnore
  @BeanProperty var userId: UUID = _
  @BeanProperty var name: String = _

  def this(userId: UUID, name: String) = {
    this()
    this.userId = userId
    this.name = name
  }
}


@Accessor trait UserAccessor {
  @Query("SELECT * FROM users") def getAll: Result[User]
  @Query("TRUNCATE users") def truncate: Result[User]
}


